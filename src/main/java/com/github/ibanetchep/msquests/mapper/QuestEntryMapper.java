package com.github.ibanetchep.msquests.mapper;

import com.github.ibanetchep.msquests.annotation.ObjectiveDefinitionType;
import com.github.ibanetchep.msquests.database.dto.QuestDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.quest.*;
import com.github.ibanetchep.msquests.registry.ObjectiveTypeRegistry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestEntryMapper {

    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestEntryMapper(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public Quest toEntity(QuestDTO dto, QuestActor actor, QuestDefinition questDefinition) {
        Quest quest = new Quest(
                dto.id(),
                questDefinition,
                actor,
                dto.status(),
                new Date(dto.startedAt()),
                new Date(dto.expiresAt()),
                new Date(dto.completedAt()),
                new Date(dto.createdAt()),
                new Date(dto.updatedAt())
        );

        Map<UUID, QuestObjective<?>> objectives = new HashMap<>();
        
        if (dto.objectives() != null) {
            for (Map.Entry<UUID, QuestObjectiveDTO> entry : dto.objectives().entrySet()) {
                QuestObjectiveDTO objectiveDto = entry.getValue();
                
                // Get the corresponding objective definition from the quest
                QuestObjectiveDefinition objectiveDefinition = questDefinition.getObjectives().get(objectiveDto.objectiveId());
                if (objectiveDefinition == null) {
                    throw new IllegalStateException("Objective definition not found for ID: " + objectiveDto.objectiveId());
                }

                // Get the objective type corresponding to the definition
                String objectiveType = objectiveDefinition.getClass().getAnnotation(ObjectiveDefinitionType.class).type();
                Class<? extends QuestObjective<?>> objectiveClass = objectiveTypeRegistry.getObjectiveClass(objectiveType);
                if (objectiveClass == null) {
                    throw new IllegalStateException("Objective type not found: " + objectiveType);
                }

                try {
                    // Create a new instance of the objective with the correct values
                    QuestObjective<?> objective = objectiveClass.getConstructor(
                            UUID.class,
                            Quest.class,
                            int.class,
                            objectiveDefinition.getClass()
                    ).newInstance(
                            objectiveDto.id(),
                            quest,
                            objectiveDto.progress(),
                            objectiveDefinition
                    );

                    objectives.put(objectiveDto.id(), objective);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Error creating objective of type " + objectiveType, e);
                }
            }
        }

        quest.setObjectives(objectives);
        return quest;
    }

    public QuestDTO toDto(Quest entity) {
        Map<UUID, QuestObjectiveDTO> objectiveDtos = new HashMap<>();
        
        if (entity.getObjectives() != null) {
            for (Map.Entry<UUID, QuestObjective<?>> entry : entity.getObjectives().entrySet()) {
                QuestObjective<?> objective = entry.getValue();
                
                objectiveDtos.put(entry.getKey(), new QuestObjectiveDTO(
                        objective.getId(),
                        entity.getId(),
                        objective.getObjectiveDefinition().getId(),
                        objective.getProgress(),
                        objective.getStatus(),
                        objective.getStartedAt().getTime(),
                        objective.getCompletedAt().getTime(),
                        objective.getCreatedAt().getTime(),
                        objective.getUpdatedAt().getTime()
                ));
            }
        }

        return new QuestDTO(
                entity.getId(),
                entity.getQuest().getId(),
                entity.getActor().getId(),
                entity.getStatus(),
                entity.getStartedAt().getTime(),
                entity.getCompletedAt().getTime(),
                entity.getExpiresAt().getTime(),
                entity.getCreatedAt().getTime(),
                entity.getUpdatedAt().getTime(),
                objectiveDtos
        );
    }
}