package com.github.ibanetchep.msquests.core.dto;

import java.util.Map;

public record QuestObjectiveConfigDTO(
        String key,
        String type,
        Map<String, Object> config
) {}