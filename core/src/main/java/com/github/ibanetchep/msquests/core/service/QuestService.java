package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class QuestService {

    private final Logger logger;
    private final QuestConfigRegistry questConfigRegistry;
    private final QuestRepository questRepository;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;
    private final QuestMapper questMapper;

    public QuestService(
            Logger logger,
            QuestConfigRegistry questConfigRegistry,
            QuestRepository questRepository,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            QuestMapper questMapper
    ) {
        this.logger = logger;
        this.questConfigRegistry = questConfigRegistry;
        this.questRepository = questRepository;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.questMapper = questMapper;
    }

    public CompletableFuture<Void> loadQuests(QuestActor actor) {
        return questRepository.getAllByActor(actor.getId())
                .thenAccept(questEntryDtos -> {
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

                        Quest quest = questFactory.createQuest(questConfig, actor, questDTO);
                        questRegistry.add(quest);
                    }
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
}
