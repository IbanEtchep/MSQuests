package com.github.ibanetchep.msquests.core.dto;

import java.util.UUID;

public record QuestObjectiveDefinitionDTO(
        UUID id,
        String type,
        String config
) {}