package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;

import java.util.ArrayList;
import java.util.List;

public class QuestConfigMapper {

    private final QuestObjectiveFactory questObjectiveFactory;
    private final QuestActionFactory rewardTypeRegistry;

    public QuestConfigMapper(QuestObjectiveFactory questObjectiveFactory, QuestActionFactory rewardTypeRegistry) {
        this.questObjectiveFactory = questObjectiveFactory;
        this.rewardTypeRegistry = rewardTypeRegistry;
    }

    public QuestConfigDTO toDTO(QuestConfig entity) {
        List<QuestStageConfigDTO> stageDtos = new ArrayList<>();

        for (QuestStageConfig stageConfig : entity.getStages().values()) {
            List<QuestObjectiveConfigDTO> objectiveDtos = new ArrayList<>();

            for (QuestObjectiveConfig objectiveConfig : stageConfig.getObjectives().values()) {
                objectiveDtos.add(objectiveConfig.toDTO());
            }

            stageDtos.add(new QuestStageConfigDTO(stageConfig.getKey(), stageConfig.getName(), stageConfig.getFlow(), objectiveDtos));
        }

        List<QuestActionDTO> rewards = new ArrayList<>();
        if (entity.getRewards() != null) {
            for (QuestAction questAction : entity.getRewards()) {
                rewards.add(questAction.toDTO());
            }
        }

        return new QuestConfigDTO(
                entity.getKey(),
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

        for (QuestStageConfigDTO stageDto : dto.stages()) {
            QuestStageConfig stageConfig = new QuestStageConfig(stageDto.key(), stageDto.name(), stageDto.flow());
            questConfig.addStage(stageConfig);

            for (QuestObjectiveConfigDTO objectiveDto : stageDto.objectives()) {
                QuestObjectiveConfig objectiveConfig = questObjectiveFactory.createConfig(objectiveDto);
                stageConfig.addObjective(objectiveConfig);
            }
        }

        return questConfig;
    }
}