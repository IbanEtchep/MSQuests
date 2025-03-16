package com.github.ibanetchep.msquests.mapper;

import com.github.ibanetchep.msquests.annotation.ObjectiveDefinitionType;
import com.github.ibanetchep.msquests.database.dto.QuestDefinitionDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDefinitionDTO;
import com.github.ibanetchep.msquests.model.quest.QuestDefinition;
import com.github.ibanetchep.msquests.model.quest.QuestObjectiveDefinition;
import com.github.ibanetchep.msquests.model.quest.QuestReward;
import com.github.ibanetchep.msquests.registry.ObjectiveDefinitionTypeRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuestDefinitionMapper {

    private final Gson gson = new Gson();
    private final ObjectiveDefinitionTypeRegistry objectiveTypeRegistry;

    public QuestDefinitionMapper(ObjectiveDefinitionTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public QuestDefinitionDTO toDto(QuestDefinition entity) {
        Map<UUID, QuestObjectiveDefinitionDTO> objectiveDtos = new HashMap<>();
        
        if (entity.getObjectives() != null) {
            for (Map.Entry<UUID, QuestObjectiveDefinition> entry : entity.getObjectives().entrySet()) {
                QuestObjectiveDefinition objective = entry.getValue();
                String type = objective.getClass().getAnnotation(ObjectiveDefinitionType.class).type();
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
                entity.getCreatedAt().getTime(),
                entity.getUpdatedAt().getTime(),
                objectiveDtos
        );
    }

    public QuestDefinition toEntity(QuestDefinitionDTO dto) {
        QuestDefinition questDefinition = new QuestDefinition(dto.uniqueId(), dto.name(), dto.description(), dto.duration());

        try {
            Set<String> tags = gson.fromJson(dto.tags(), new TypeToken<Set<String>>(){}.getType());
            questDefinition.setTags(tags);

            List<QuestReward> rewards = gson.fromJson(dto.rewards(), new TypeToken<List<QuestReward>>(){}.getType());
            questDefinition.setRewards(rewards);

            Map<UUID, QuestObjectiveDefinition> objectives = new HashMap<>();
            for (Map.Entry<UUID, QuestObjectiveDefinitionDTO> entry : dto.objectives().entrySet()) {
                QuestObjectiveDefinitionDTO objectiveDto = entry.getValue();
                Class<? extends QuestObjectiveDefinition> type = objectiveTypeRegistry.getType(objectiveDto.type());
                if (type == null) {
                    throw new IllegalArgumentException("Type d'objectif non trouvé : " + objectiveDto.type());
                }
                QuestObjectiveDefinition objective = gson.fromJson(objectiveDto.config(), type);
                objectives.put(entry.getKey(), objective);
            }
            questDefinition.setObjectives(objectives);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Erreur lors du parsing des données JSON", e);
        }

        return questDefinition;
    }
}