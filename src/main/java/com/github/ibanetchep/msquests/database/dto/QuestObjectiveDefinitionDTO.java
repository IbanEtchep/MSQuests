package com.github.ibanetchep.msquests.database.dto;

import java.util.UUID;

public record QuestObjectiveDefinitionDTO(
        UUID uniqueId,
        String type,
        String config
) {}