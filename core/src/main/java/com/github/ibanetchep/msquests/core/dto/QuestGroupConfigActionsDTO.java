package com.github.ibanetchep.msquests.core.dto;

import java.util.List;

/** The group's hooks, each an Artisan action chain in keyed form (see ActionChain). */
public record QuestGroupConfigActionsDTO(
        List<Object> questStart,
        List<Object> questComplete,
        List<Object> objectiveProgress,
        List<Object> objectiveComplete,
        List<Object> questDistribution,
        List<Object> actorLoad,
        List<Object> allPeriodQuestsComplete
) {
}
