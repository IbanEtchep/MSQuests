package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestConfigMapper {

    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestConfigMapper(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public QuestConfigDTO toDto(QuestConfig entity) {
        Map<String, QuestObjectiveConfigDTO> objectiveDtos = new HashMap<>();

        if (entity.getObjectives() != null) {
            for (Map.Entry<String, QuestObjectiveConfig> entry : entity.getObjectives().entrySet()) {
                QuestObjectiveConfig objective = entry.getValue();
                String type = objective.getType();
                Map<String, Object> config = objective.serialize();
                config.put("type", type);
                objectiveDtos.put(entry.getKey(), new QuestObjectiveConfigDTO(entry.getKey(), config));
            }
        }

        return new QuestConfigDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getDuration(),
                List.copyOf(entity.getTags()),
                List.copyOf(entity.getRewards()),
                objectiveDtos
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(
                dto.key(), dto.name(), dto.description(), dto.duration()
        );

        if (dto.tags() != null) {
            questConfig.getTags().addAll(dto.tags());
        }

        if (dto.rewards() != null) {
            questConfig.getRewards().addAll(dto.rewards());
        }

        for (Map.Entry<String, QuestObjectiveConfigDTO> entry : dto.objectives().entrySet()) {
            QuestObjectiveConfigDTO objectiveDto = entry.getValue();
            QuestObjectiveConfig objective = objectiveTypeRegistry.createConfig(
                    objectiveDto.config()
            );

            questConfig.addObjective(objective);
        }

        return questConfig;
    }
}