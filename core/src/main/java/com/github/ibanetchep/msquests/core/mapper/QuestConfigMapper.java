package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ActionTypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestConfigMapper {

    private final ObjectiveTypeRegistry objectiveTypeRegistry;
    private final ActionTypeRegistry rewardTypeRegistry;

    public QuestConfigMapper(ObjectiveTypeRegistry objectiveTypeRegistry, ActionTypeRegistry rewardTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
        this.rewardTypeRegistry = rewardTypeRegistry;
    }

    public QuestConfigDTO toDto(QuestConfig entity) {
        Map<String, QuestObjectiveConfigDTO> objectiveDtos = new HashMap<>();

        if (entity.getObjectives() != null) {
            for (Map.Entry<String, QuestObjectiveConfig> entry : entity.getObjectives().entrySet()) {
                QuestObjectiveConfig objective = entry.getValue();
                objectiveDtos.put(entry.getKey(), objective.toDTO());
            }
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
                entity.getFlow(),
                List.copyOf(entity.getTags()),
                rewards,
                objectiveDtos
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(
                dto.key(), dto.name(), dto.description(), dto.duration(), dto.flow()
        );

        if (dto.tags() != null) {
            questConfig.getTags().addAll(dto.tags());
        }

        if (dto.rewards() != null) {
            for (QuestActionDTO rewardDto : dto.rewards()) {
                QuestAction questAction = rewardTypeRegistry.createAction(rewardDto);
                questConfig.addReward(questAction);
            }
        }

        for (Map.Entry<String, QuestObjectiveConfigDTO> entry : dto.objectives().entrySet()) {
            QuestObjectiveConfigDTO objectiveDto = entry.getValue();
            QuestObjectiveConfig objective = objectiveTypeRegistry.createConfig(objectiveDto);
            questConfig.addObjective(objective);
        }

        return questConfig;
    }
}