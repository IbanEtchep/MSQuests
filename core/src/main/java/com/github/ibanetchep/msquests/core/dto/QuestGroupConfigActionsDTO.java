package com.github.ibanetchep.msquests.core.dto;

import java.util.List;

public record QuestGroupConfigActionsDTO(
        List<QuestActionDTO> questStart,
        List<QuestActionDTO> questComplete,
        List<QuestActionDTO> objectiveProgress,
        List<QuestActionDTO> objectiveComplete
) {
}
