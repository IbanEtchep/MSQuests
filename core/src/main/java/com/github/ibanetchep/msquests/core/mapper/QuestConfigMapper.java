package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.QuestReward;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class QuestConfigMapper {

    private final Gson gson = new Gson();
    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestConfigMapper(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public QuestConfigDTO toDto(QuestConfig entity) {
        Map<String, QuestObjectiveConfigDTO> objectiveDtos = new HashMap<>();

        if (entity.getObjectives() != null) {
            for (Map.Entry<String, QuestObjectiveConfig> entry : entity.getObjectives().entrySet()) {
                QuestObjectiveConfig objective = entry.getValue();
                ObjectiveType typeAnnotation = objective.getClass().getAnnotation(ObjectiveType.class);
                String type = Objects.requireNonNull(typeAnnotation).type();
                Map<String, Object> config = objective.serialize();

                objectiveDtos.put(entry.getKey(), new QuestObjectiveConfigDTO(entry.getKey(), type, config));
            }
        }

        return new QuestConfigDTO(
                entity.getKey(),
                entity.getName(),
                entity.getDescription(),
                gson.toJson(entity.getTags()),
                gson.toJson(entity.getRewards()),
                entity.getDuration(),
                objectiveDtos
        );
    }

    public QuestConfig toEntity(QuestConfigDTO dto) {
        QuestConfig questConfig = new QuestConfig(
                dto.key(), dto.name(), dto.description(), dto.duration()
        );

        Set<String> tags = gson.fromJson(dto.tags(), new TypeToken<Set<String>>(){}.getType());
        questConfig.setTags(tags);

        //TODO rewards

        for (Map.Entry<String, QuestObjectiveConfigDTO> entry : dto.objectives().entrySet()) {
            QuestObjectiveConfigDTO objectiveDto = entry.getValue();
            QuestObjectiveConfig objective = objectiveTypeRegistry.createConfig(
                    objectiveDto.type(),
                    objectiveDto.config()
            );

            questConfig.addObjective(objective);
        }

        return questConfig;
    }
}