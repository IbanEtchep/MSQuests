package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.QuestObjectiveStatus;

import java.util.UUID;

public record QuestObjectiveDTO(
        UUID id,
        UUID questEntryId,
        String objectiveKey,
        int progress,
        QuestObjectiveStatus objectiveStatus,
        long startedAt,
        long completedAt,
        long createdAt,
        long updatedAt
) {}