package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestPersistenceService {

    private final Logger logger;
    private final QuestRegistry questRegistry;
    private final QuestConfigRepository questConfigRepository;
    private final ActorRepository actorRepository;
    private final QuestRepository questRepository;
    private final QuestGroupMapper questGroupMapper;
    private final QuestMapper questMapper;

    public QuestPersistenceService(
            Logger logger,
            QuestRegistry questRegistry,
            QuestConfigRepository questConfigRepository,
            ActorRepository actorRepository,
            QuestRepository questRepository,
            QuestGroupMapper questGroupMapper,
            QuestMapper questMapper
    ) {
        this.logger = logger;
        this.questRegistry = questRegistry;
        this.questConfigRepository = questConfigRepository;
        this.actorRepository = actorRepository;
        this.questRepository = questRepository;
        this.questGroupMapper = questGroupMapper;
        this.questMapper = questMapper;
    }

    public void loadQuestGroups() {
        questRegistry.clearQuestGroups();
        questConfigRepository.getAllGroups().thenAccept(questGroupDtos -> {
            for (QuestGroupDTO questGroupDTO : questGroupDtos.values()) {
                QuestGroup questGroup = questGroupMapper.toEntity(questGroupDTO);
                questRegistry.registerQuestGroup(questGroup);
                logger.info("Loaded group " + questGroup.getKey() + " with " + questGroup.getQuestConfigs().size() + " quests.");
            }
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load quest groups", e);
            return null;
        });
    }

    public void loadActor(QuestActor actor) {
        questRegistry.removeActor(actor.getId());

        actorRepository.get(actor.getId()).thenAccept(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(actor.getActorType(), actor.getId());
                actorRepository.add(actorDTO);
            }

            questRegistry.registerActor(actor);

            loadQuests(actor);
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load actor", e);
            return null;
        });
    }

    private void loadQuests(QuestActor actor) {
        questRepository.getAllByActor(actor.getId()).thenAccept(questEntryDtos -> {
            for (QuestDTO questDTO : questEntryDtos.values()) {
                QuestGroup questGroup = questRegistry.getQuestGroups().get(questDTO.groupKey());

                if (questGroup == null) {
                    logger.warning("Could not find group " + questDTO.groupKey() + " for quest " + questDTO.questKey());
                    continue;
                }

                QuestConfig questConfig = questGroup.getQuestConfigs().get(questDTO.questKey());

                if (questConfig == null) {
                    logger.warning("Could not find config for quest " + questDTO.questKey() + " in group " + questDTO.groupKey());
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
