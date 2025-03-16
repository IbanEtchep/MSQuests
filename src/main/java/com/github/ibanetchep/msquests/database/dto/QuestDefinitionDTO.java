package com.github.ibanetchep.msquests.database.dto;

import java.util.Map;
import java.util.UUID;

public record QuestDefinitionDTO(
        UUID id,
        String name,
        String description,
        String tags,
        String rewards,
        long duration,
        Map<UUID, QuestObjectiveDefinitionDTO> objectives
) {}