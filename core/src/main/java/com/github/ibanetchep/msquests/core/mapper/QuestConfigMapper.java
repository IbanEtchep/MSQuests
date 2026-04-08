package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.factory.ConditionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestActionFactory;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestConfigMapper {

    private final QuestObjectiveFactory questObjectiveFactory;
    private final QuestActionFactory rewardTypeRegistry;
    private final ConditionFactory conditionFactory;

    public QuestConfigMapper(QuestObjectiveFactory questObjectiveFactory, QuestActionFactory rewardTypeRegistry, ConditionFactory conditionFactory) {
        this.questObjectiveFactory = questObjectiveFactory;
        this.rewardTypeRegistry = rewardTypeRegistry;
        this.conditionFactory = conditionFactory;
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
                entity.getTier(),
                rewards,
                stageDtos,
                null
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(dto.key(), dto.name(), dto.description(), dto.duration());
        questConfig.setTier(dto.tier());

        if (dto.rewards() != null) {
            for (QuestActionDTO rewardDto : dto.rewards()) {
                QuestAction questAction = rewardTypeRegistry.createAction(rewardDto);
                questConfig.addReward(questAction);
            }
        }

        if (dto.conditions() != null) {
            List<Condition> conditions = dto.conditions().stream()
                    .map(conditionFactory::build)
                    .filter(Objects::nonNull)
                    .toList();
            questConfig.setConditions(conditions);
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