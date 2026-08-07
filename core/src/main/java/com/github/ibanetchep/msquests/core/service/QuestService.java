package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
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
import java.util.List;
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

    public CompletableFuture<Void> loadQuests(QuestActor actor) {
        return questRepository.getAllByActor(actor.getId())
                .thenCompose(questEntryDtos -> {
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

                        Quest quest = questFactory.createQuest(questConfig, actor, questDTO);
                        questRegistry.add(quest);
                    }

                    return loadRotationCounts(actor);
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

    private CompletableFuture<Void> loadRotationCounts(QuestActor actor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (QuestGroupConfig groupConfig : questConfigRegistry.getQuestGroupConfigs().values()) {
            if (!groupConfig.isRotatable()) continue;

            ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
            if (actorGroup == null) continue;

            Instant periodStart = groupConfig.getPeriodStart();
            Instant periodEnd = groupConfig.getPeriodEnd();

            CompletableFuture<Void> future = rotationRepository.countInPeriod(actor.getId(), groupConfig.getKey(), periodStart, periodEnd)
                    .thenAccept(count -> {
                        for (int i = 0; i < count; i++) {
                            actorGroup.incrementRotations();
                        }
                    })
                    .exceptionally(e -> {
                        logger.log(Level.WARNING, "Failed to load rotation count for group " + groupConfig.getKey(), e);
                        return null;
                    });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
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
