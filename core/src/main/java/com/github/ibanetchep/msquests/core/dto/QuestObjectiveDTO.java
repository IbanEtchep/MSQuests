package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

import java.util.UUID;

public record QuestObjectiveDTO(
        UUID questEntryId,
        String objectiveKey,
        String objectiveType,
        QuestObjectiveStatus objectiveStatus,
        int progress
) {}