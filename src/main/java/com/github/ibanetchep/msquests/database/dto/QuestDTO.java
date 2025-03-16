package com.github.ibanetchep.msquests.database.dto;

import com.github.ibanetchep.msquests.model.quest.QuestStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record QuestDTO(
        UUID id,
        UUID questId,
        UUID actorId,
        QuestStatus status,
        long startedAt,
        long completedAt,
        long expiresAt,
        long createdAt,
        long updatedAt,
        Map<UUID, QuestObjectiveDTO> objectives
) {
    public QuestDTO {
        objectives = objectives == null ? new HashMap<>() : objectives;
    }
}