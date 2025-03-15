package com.github.ibanetchep.msquests.database.dto;

import com.github.ibanetchep.msquests.model.QuestStatus;

import java.util.UUID;

public record QuestEntryDTO(
        UUID uniqueId,
        UUID questId,
        QuestStatus status,
        long startedAt,
        long expiresAt,
        long createdAt,
        long updatedAt,
        long completedAt,
        UUID actorId
) {}