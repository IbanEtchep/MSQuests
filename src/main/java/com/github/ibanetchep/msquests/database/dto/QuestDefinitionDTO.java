package com.github.ibanetchep.msquests.database.dto;

import java.util.Map;
import java.util.UUID;

public record QuestDefinitionDTO(
        UUID uniqueId,
        String name,
        String description,
        String tags,
        String rewards,
        long duration,
        long createdAt,
        long updatedAt,
        Map<UUID, QuestObjectiveDefinitionDTO> objectives
) {}