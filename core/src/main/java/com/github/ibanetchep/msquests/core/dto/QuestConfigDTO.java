package com.github.ibanetchep.msquests.core.dto;

import java.util.Map;

public record QuestConfigDTO(
        String key,
        String name,
        String description,
        String tags,
        String rewards,
        long duration,
        Map<String, QuestObjectiveConfigDTO> objectives
) {}