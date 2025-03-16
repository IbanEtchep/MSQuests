package com.github.ibanetchep.msquests.mapper;

import com.github.ibanetchep.msquests.database.dto.QuestDTO;
import com.github.ibanetchep.msquests.database.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.model.quest.QuestDefinition;
import com.github.ibanetchep.msquests.model.quest.Quest;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.quest.QuestObjective;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuestEntryMapper {

    public static Quest toEntity(QuestDTO dto, QuestActor actor, QuestDefinition quest) {
        Map<UUID, QuestObjective> objectives = dto.objectives().values().stream()
                .map(QuestObjectiveEntryMapper::toEntity)
                .collect(Collectors.toMap(QuestObjective::getId, Function.identity()));

        return new Quest(
                dto.uniqueId(),
                quest,
                actor,
                dto.status(),
                new Date(dto.startedAt()),
                new Date(dto.expiresAt()),
                new Date(dto.completedAt()),
                new Date(dto.createdAt()),
                new Date(dto.updatedAt())
        );
    }

    public QuestDTO toDto(Quest entity) {
        Map<UUID, QuestObjectiveDTO> objectiveDtos = entity.getObjectives().values().stream()
                .map(QuestObjectiveEntryMapper::toDto)
                .collect(Collectors.toMap(QuestObjectiveDTO::uniqueId, Function.identity()));

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