package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestConfigMapper {

    private final QuestObjectiveFactory questObjectiveFactory;
    private final QuestActionFactory rewardTypeRegistry;

    public QuestConfigMapper(QuestObjectiveFactory questObjectiveFactory, QuestActionFactory rewardTypeRegistry) {
        this.questObjectiveFactory = questObjectiveFactory;
        this.rewardTypeRegistry = rewardTypeRegistry;
    }

    public QuestConfigDTO toDTO(QuestConfig entity) {
        Map<String, QuestStageConfigDTO> stageDtos = new HashMap<>();

        for (QuestStageConfig stageConfig : entity.getStages().values()) {
            Map<String, QuestObjectiveConfigDTO> objectiveDtos = new HashMap<>();

            for (QuestObjectiveConfig objectiveConfig : stageConfig.getObjectives().values()) {
                objectiveDtos.put(objectiveConfig.getKey(), objectiveConfig.toDTO());
            }

            stageDtos.put(stageConfig.getKey(), new QuestStageConfigDTO(stageConfig.getKey(), stageConfig.getName(), stageConfig.getFlow(), objectiveDtos));
        }

        List<QuestActionDTO> rewards = new ArrayList<>();
        if (entity.getRewards() != null) {
            for (QuestAction questAction : entity.getRewards()) {
                rewards.add(questAction.toDTO());
            }
        }

        return new QuestConfigDTO(
                entity.getKey(),
                entity.getGroup().getKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getDuration(),
                rewards,
                stageDtos
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(dto.key(), dto.name(), dto.description(), dto.duration());

        if (dto.rewards() != null) {
            for (QuestActionDTO rewardDto : dto.rewards()) {
                QuestAction questAction = rewardTypeRegistry.createAction(rewardDto);
                questConfig.addReward(questAction);
            }
        }

        for (Map.Entry<String, QuestStageConfigDTO> entry : dto.stages().entrySet()) {
            QuestStageConfigDTO stageDto = entry.getValue();

            QuestStageConfig stageConfig = new QuestStageConfig(stageDto.key(), stageDto.name(), stageDto.flow());
            questConfig.addStage(stageConfig);

            for (Map.Entry<String, QuestObjectiveConfigDTO> objectiveEntry : stageDto.objectives().entrySet()) {
                QuestObjectiveConfigDTO objectiveDto = objectiveEntry.getValue();

                QuestObjectiveConfig objectiveConfig = questObjectiveFactory.createConfig(objectiveDto);
                stageConfig.addObjective(objectiveConfig);
            }
        }

        return questConfig;
    }
}