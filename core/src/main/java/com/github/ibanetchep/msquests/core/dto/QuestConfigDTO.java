package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.objective.Flow;

import java.util.List;
import java.util.Map;

public record QuestConfigDTO(
        String key,
        String groupKey,
        String name,
        String description,
        long duration,
        Flow flow,
        List<String> tags,
        List<QuestActionDTO> rewards,
        Map<String, QuestObjectiveConfigDTO> objectives
) {}