package com.github.ibanetchep.msquests.database.dto;

import java.util.UUID;

public record QuestDTO(
        UUID uniqueId,
        String name,
        String description,
        String tags,
        String rewards,
        long duration,
        long createdAt,
        long updatedAt
) {}