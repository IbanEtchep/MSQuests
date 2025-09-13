package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.QuestStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record QuestDTO(
        UUID id,
        String questKey,
        String groupKey,
        UUID actorId,
        QuestStatus status,
        Long completedAt,
        Long createdAt,
        Long updatedAt,
        Map<String, QuestObjectiveDTO> objectives
) {
    public QuestDTO {
        objectives = objectives == null ? new HashMap<>() : objectives;
    }
}