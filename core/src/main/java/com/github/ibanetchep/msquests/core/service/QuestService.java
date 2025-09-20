package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestService {

    private final Logger logger;
    private final QuestConfigRegistry questRegistry;
    private final QuestRepository questRepository;
    private final QuestMapper questMapper;

    public QuestService(
            Logger logger,
            QuestConfigRegistry questRegistry,
            ActorRepository actorRepository,
            QuestRepository questRepository,
            QuestMapper questMapper
    ) {
        this.logger = logger;
        this.questRegistry = questRegistry;
        this.questRepository = questRepository;
        this.questMapper = questMapper;
    }


    public CompletableFuture<Void> loadQuests(QuestActor actor) {
        return questRepository.getAllByActor(actor.getId())
                .thenAccept(questEntryDtos -> {
                    for (QuestDTO questDTO : questEntryDtos.values()) {
                        QuestGroupConfig questGroupConfig = questRegistry.getQuestGroupConfigs().get(questDTO.groupKey());

                        if (questGroupConfig == null) {
                            logger.warning("Could not find group " + questDTO.groupKey() + " for quest " + questDTO.questKey());
                            continue;
                        }

                        QuestConfig questConfig = questGroupConfig.getQuestConfigs().get(questDTO.questKey());
                        if (questConfig == null) {
                            logger.warning("Could not find config for quest " + questDTO.questKey()
                                    + " in group " + questDTO.groupKey());
                            continue;
                        }

                        Quest quest = questMapper.toEntity(questDTO, actor, questConfig);
                        actor.addQuest(quest);
                    }
                });
    }


    public void saveQuest(Quest quest) {
        questRepository.save(questMapper.toDto(quest)).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to save quest", e);
            return null;
        });
    }
}
