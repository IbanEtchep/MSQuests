package com.github.ibanetchep.msquests.database.dto;

import com.github.ibanetchep.msquests.model.QuestObjectiveStatus;

import java.util.UUID;

public record QuestObjectiveEntryDTO(
        UUID uniqueId,
        UUID questEntryId,
        UUID objectiveId,
        int progress,
        QuestObjectiveStatus objectiveStatus,
        long startedAt,
        long completedAt,
        long createdAt,
        long updatedAt
) {}