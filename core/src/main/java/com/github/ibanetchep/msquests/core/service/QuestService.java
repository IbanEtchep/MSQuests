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
import java.util.concurrent.CompletableFuture;
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
                            logger.warning("Could not find group " + questDTO.groupKey() + " for quest " + questDTO.questKey());
                            continue;
                        }

                        QuestConfig questConfig = questGroupConfig.getQuestConfigs().get(questDTO.questKey());
                        if (questConfig == null) {
                            logger.warning("Could not find params for quest " + questDTO.questKey()
                                    + " in group " + questDTO.groupKey());
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
