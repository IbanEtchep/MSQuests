package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestConfigMapper {

    private final QuestObjectiveFactory questObjectiveFactory;

    public QuestConfigMapper(QuestObjectiveFactory questObjectiveFactory) {
        this.questObjectiveFactory = questObjectiveFactory;
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

        return new QuestConfigDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getDuration(),
                entity.getTier(),
                entity.getRewards().steps(),
                entity.getRewardDisplay(),
                stageDtos,
                entity.getRequirements().isEmpty() ? null : entity.getRequirements().conditions(),
                entity.getUid()
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(dto.key(), dto.name(), dto.description(),
                dto.duration() != null ? dto.duration() : 0L); // absent: no limit, instead of an unboxing NPE
        questConfig.setTier(dto.tier());
        questConfig.setUid(dto.uid());

        questConfig.setRewards(ActionChain.of(dto.rewards()));
        if (dto.rewardDisplay() != null) {
            questConfig.setRewardDisplay(dto.rewardDisplay());
        }
        questConfig.setRequirements(Conditions.of(dto.requirements()));

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