package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.mapper.QuestGroupMapper;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestConfigService {

    private final Logger logger;
    private final QuestConfigRegistry questRegistry;
    private final QuestConfigRepository questConfigRepository;
    private final QuestGroupMapper questGroupMapper;

    public QuestConfigService(
            Logger logger,
            QuestConfigRegistry questRegistry,
            QuestConfigRepository questConfigRepository,
            QuestGroupMapper questGroupMapper
    ) {
        this.logger = logger;
        this.questRegistry = questRegistry;
        this.questConfigRepository = questConfigRepository;
        this.questGroupMapper = questGroupMapper;
    }

    public CompletableFuture<Void> loadQuestGroups() {
        questRegistry.clearQuestGroupConfigs();

        return questConfigRepository.getAllGroups()
                .thenAccept(questGroupDtos -> {
                    for (QuestGroupDTO questGroupDTO : questGroupDtos.values()) {
                        QuestGroupConfig questGroupConfig = questGroupMapper.toEntity(questGroupDTO);
                        questRegistry.registerQuestGroupConfig(questGroupConfig);
                        logger.info("Loaded group " + questGroupConfig.getKey() + " with "
                                + questGroupConfig.getQuestConfigs().size() + " quests.");
                    }
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to load quest groups", e);
                    return null;
                });
    }

}
