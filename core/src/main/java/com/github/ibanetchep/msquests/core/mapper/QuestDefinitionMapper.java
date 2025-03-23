package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDefinitionDTO;
import com.github.ibanetchep.msquests.core.quest.QuestDefinition;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveDefinition;
import com.github.ibanetchep.msquests.core.quest.QuestReward;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class QuestDefinitionMapper {

    private final Gson gson = new Gson();
    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestDefinitionMapper(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public QuestDefinitionDTO toDto(QuestDefinition entity) {
        Map<UUID, QuestObjectiveDefinitionDTO> objectiveDtos = new HashMap<>();
        
        if (entity.getObjectives() != null) {
            for (Map.Entry<UUID, QuestObjectiveDefinition> entry : entity.getObjectives().entrySet()) {
                QuestObjectiveDefinition objective = entry.getValue();
                String type = objective.getClass().getAnnotation(ObjectiveType.class).type();
                String config = gson.toJson(objective);
                objectiveDtos.put(entry.getKey(), new QuestObjectiveDefinitionDTO(entry.getKey(), type, config));
            }
        }

        return new QuestDefinitionDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                gson.toJson(entity.getTags()),
                gson.toJson(entity.getRewards()),
                entity.getDuration(),
                objectiveDtos
        );
    }

    public QuestDefinition toEntity(QuestDefinitionDTO dto) {
        QuestDefinition questDefinition = new QuestDefinition(
                dto.id(), dto.name(), dto.description(), dto.duration()
        );

        try {
            Set<String> tags = gson.fromJson(dto.tags(), new TypeToken<Set<String>>(){}.getType());
            questDefinition.setTags(tags);

            List<QuestReward> rewards = gson.fromJson(dto.rewards(), new TypeToken<List<QuestReward>>(){}.getType());
            questDefinition.setRewards(rewards);

            Map<UUID, QuestObjectiveDefinition> objectives = new HashMap<>();
            for (Map.Entry<UUID, QuestObjectiveDefinitionDTO> entry : dto.objectives().entrySet()) {
                QuestObjectiveDefinitionDTO objectiveDto = entry.getValue();
                Class<? extends QuestObjectiveDefinition> definitionClass = objectiveTypeRegistry.getDefinitionClass(objectiveDto.type());
                if (definitionClass == null) {
                    throw new IllegalArgumentException("Objective type not found: " + objectiveDto.type());
                }
                QuestObjectiveDefinition objective = gson.fromJson(objectiveDto.config(), definitionClass);
                objectives.put(entry.getKey(), objective);
            }
            questDefinition.setObjectives(objectives);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Error parsing JSON data", e);
        }

        return questDefinition;
    }
}