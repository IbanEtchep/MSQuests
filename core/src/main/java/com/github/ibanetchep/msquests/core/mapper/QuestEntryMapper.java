package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestEntryMapper {

    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestEntryMapper(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
    }

    public Quest toEntity(QuestDTO dto, QuestActor actor, QuestConfig questConfig) {
        Quest quest = new Quest(
                dto.id(),
                questConfig,
                actor,
                dto.status(),
                new Date(dto.startedAt()),
                new Date(dto.expiresAt()),
                new Date(dto.completedAt()),
                new Date(dto.createdAt()),
                new Date(dto.updatedAt())
        );

        if (dto.objectives() != null) {
            for (Map.Entry<UUID, QuestObjectiveDTO> entry : dto.objectives().entrySet()) {
                QuestObjectiveDTO objectiveDto = entry.getValue();
                
                QuestObjectiveConfig questObjectiveConfig = questConfig.getObjectives().get(objectiveDto.objectiveKey());
                if (questObjectiveConfig == null) {
                    throw new IllegalStateException("Objective definition not found for ID: " + objectiveDto.objectiveKey());
                }

                String objectiveType = questObjectiveConfig.getType();
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
                            questObjectiveConfig.getClass()
                    ).newInstance(
                            objectiveDto.id(),
                            quest,
                            objectiveDto.progress(),
                            questObjectiveConfig
                    );

                    quest.addObjective(objective);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Error creating objective of type " + objectiveType, e);
                }
            }
        }
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
                        objective.getObjectiveConfig().getKey(),
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
                entity.getQuest().getKey(),
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