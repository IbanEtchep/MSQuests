package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestService {

    private final Logger logger;
    private final QuestConfigRegistry questConfigRegistry;
    private final QuestRepository questRepository;
    private final RotationRepository rotationRepository;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;
    private final QuestMapper questMapper;

    /**
     * Config keys already reported as missing. A quest dropped from the YAML is orphaned for
     * every actor that ever had it, so without this the same fact is logged once per player,
     * on every load. Rows are deliberately kept — pruning them would turn any config loading
     * glitch into permanent data loss.
     */
    private final Set<String> reportedMissingConfigs = ConcurrentHashMap.newKeySet();

    public QuestService(
            Logger logger,
            QuestConfigRegistry questConfigRegistry,
            QuestRepository questRepository,
            RotationRepository rotationRepository,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            QuestMapper questMapper
    ) {
        this.logger = logger;
        this.questConfigRegistry = questConfigRegistry;
        this.questRepository = questRepository;
        this.rotationRepository = rotationRepository;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.questMapper = questMapper;
    }

    /**
     * Loads, or reloads, every stored quest of the actor. The actor's quests are replaced in one
     * step once everything has been read, so a reload neither empties the actor meanwhile nor
     * leaves the previous instances (and their stale group config) next to the new ones.
     */
    public CompletableFuture<Void> loadQuests(QuestActor actor) {
        Instant loadStartedAt = Instant.now();

        CompletableFuture<Map<QuestGroupConfig, Integer>> rotations = loadRotationCounts(actor);

        return questRepository.getAllByActor(actor.getId())
                .thenCombine(rotations, (questEntryDtos, rotationCounts) -> {
                    List<Quest> loaded = new ArrayList<>();

                    for (QuestDTO questDTO : questEntryDtos.values()) {
                        QuestGroupConfig questGroupConfig = questConfigRegistry.getQuestGroupConfigs().get(questDTO.groupKey());

                        if (questGroupConfig == null) {
                            warnMissingConfigOnce("group:" + questDTO.groupKey(),
                                    "Quest group '" + questDTO.groupKey() + "' is no longer defined in the configs"
                                            + " (first seen on quest '" + questDTO.questKey() + "').");
                            continue;
                        }

                        QuestConfig questConfig = questGroupConfig.getQuestConfigs().get(questDTO.questKey());
                        if (questConfig == null) {
                            warnMissingConfigOnce("quest:" + questDTO.groupKey() + "/" + questDTO.questKey(),
                                    "Quest '" + questDTO.questKey() + "' is no longer defined in group '"
                                            + questDTO.groupKey() + "'.");
                            continue;
                        }

                        if (!isInCurrentPeriod(questGroupConfig, questDTO)) {
                            continue;
                        }

                        loaded.add(questFactory.buildQuest(questConfig, actor, questDTO));
                    }

                    Collection<Quest> previous = actor.replaceLoadedQuests(loaded, rotationCounts, loadStartedAt);

                    // Register the new instances before dropping the old ones: pending progress is
                    // resolved by quest id, and must never find the id missing in between.
                    for (Quest quest : actor.getQuests().values()) {
                        questRegistry.add(quest);
                    }
                    for (Quest old : previous) {
                        if (!actor.getQuests().containsKey(old.getId())) {
                            questRegistry.remove(old);
                        }
                    }
                    return (Void) null;
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to load quests", e);
                    return null;
                });
    }


    /**
     * Logs a missing config once per key. The saved rows are left untouched: they are ignored
     * while the config is missing, and pick up again if it comes back.
     */
    private void warnMissingConfigOnce(String key, String message) {
        if (reportedMissingConfigs.add(key)) {
            logger.warning(message + " Its saved data is kept but ignored;"
                    + " further occurrences are not logged until the configs are reloaded.");
        }
    }

    /** Lets the next load report missing configs again, after the YAML files were re-read. */
    public void clearMissingConfigReports() {
        reportedMissingConfigs.clear();
    }

    public CompletableFuture<Void> saveQuest(Quest quest) {
        return questRepository.save(questMapper.toDTO(quest)).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to save quest", e);
            return null;
        });
    }

    /** Rotations already used this period, per rotatable group the actor can hold. */
    private CompletableFuture<Map<QuestGroupConfig, Integer>> loadRotationCounts(QuestActor actor) {
        Map<QuestGroupConfig, Integer> counts = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (QuestGroupConfig groupConfig : questConfigRegistry.getQuestGroupConfigs().values()) {
            if (!groupConfig.isRotatable()) continue;
            if (!groupConfig.getActorType().equalsIgnoreCase(actor.getActorType())) continue;

            Instant periodStart = groupConfig.getPeriodStart();
            Instant periodEnd = groupConfig.getPeriodEnd();

            CompletableFuture<Void> future = rotationRepository.countInPeriod(actor.getId(), groupConfig.getKey(), periodStart, periodEnd)
                    .thenAccept(count -> counts.put(groupConfig, count))
                    .exceptionally(e -> {
                        logger.log(Level.WARNING, "Failed to load rotation count for group " + groupConfig.getKey(), e);
                        return null;
                    });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> counts);
    }

    private boolean isInCurrentPeriod(QuestGroupConfig groupConfig, QuestDTO questDTO) {
        Instant periodStart = groupConfig.getPeriodStart();
        Instant periodEnd = groupConfig.getPeriodEnd();

        if (periodStart == null && periodEnd == null) {
            return true;
        }

        Instant createdAt = questDTO.createdAt() != null ? Instant.ofEpochMilli(questDTO.createdAt()) : null;
        if (createdAt == null) {
            return true;
        }

        return (periodStart == null || createdAt.isAfter(periodStart))
                && (periodEnd == null || createdAt.isBefore(periodEnd));
    }

    public CompletableFuture<Void> deleteQuest(Quest quest) {
        return questRepository.delete(quest.getId()).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to delete quest", e);
            return null;
        });
    }
}
