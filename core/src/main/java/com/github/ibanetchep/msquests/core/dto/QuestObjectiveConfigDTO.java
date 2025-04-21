package com.github.ibanetchep.msquests.core.dto;

import java.util.Map;

public record QuestObjectiveConfigDTO(
        String key,
        Map<String, Object> config
) {}