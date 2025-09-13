package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
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

public class QuestMapper {

    private final QuestFactory questFactory;

    public QuestMapper(QuestFactory questFactory) {
        this.questFactory = questFactory;
    }

    public Quest toEntity(QuestDTO dto, QuestActor actor, QuestConfig questConfig) {
        Quest quest = questFactory.createQuest(questConfig, actor);

        if (dto.objectives() != null) {
            for (Map.Entry<String, QuestObjectiveDTO> entry : dto.objectives().entrySet()) {
                QuestObjectiveDTO objectiveDto = entry.getValue();
                QuestObjective<?> objective = quest.getObjective(objectiveDto.objectiveKey());
                
                QuestObjectiveConfig questObjectiveConfig = questConfig.getObjectives().get(objectiveDto.objectiveKey());

                if (questObjectiveConfig == null) {
                    throw new IllegalStateException("Objective definition not found for ID: " + objectiveDto.objectiveKey());
                }

                objective.setProgress(objectiveDto.progress());
                objective.setStatus(objectiveDto.objectiveStatus());
            }
        }

        return quest;
    }

    public QuestDTO toDto(Quest entity) {
        Map<String, QuestObjectiveDTO> objectiveDtos = new HashMap<>();
        
        if (entity.getObjectives() != null) {
            for (Map.Entry<String, QuestObjective<?>> entry : entity.getObjectives().entrySet()) {
                QuestObjective<?> objective = entry.getValue();
                
                objectiveDtos.put(entry.getKey(), new QuestObjectiveDTO(
                        entity.getId(),
                        objective.getObjectiveConfig().getKey(),
                        objective.getProgress(),
                        objective.getStatus()
                ));
            }
        }

        return new QuestDTO(
                entity.getId(),
                entity.getQuestConfig().getKey(),
                entity.getQuestConfig().getGroup().getKey(),
                entity.getActor().getId(),
                entity.getStatus(),
                entity.getCompletedAt() != null ? entity.getCompletedAt().getTime() : null,
                entity.getCreatedAt().getTime(),
                entity.getUpdatedAt().getTime(),
                objectiveDtos
        );
    }
}