package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.objective.Flow;

import java.util.Map;

public record QuestStageConfigDTO(
        String key,
        String name,
        Flow flow,
        Map<String, QuestObjectiveConfigDTO> objectives
) {}