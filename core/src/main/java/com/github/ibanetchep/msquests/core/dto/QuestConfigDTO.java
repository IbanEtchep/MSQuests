package com.github.ibanetchep.msquests.core.dto;

import java.util.List;
import java.util.Map;

public record QuestConfigDTO(
        String key,
        String groupKey,
        String name,
        String description,
        long duration,
        List<String> tags,
        List<String> rewards,
        Map<String, QuestObjectiveConfigDTO> objectives
) {}