package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

import java.util.UUID;

public record QuestObjectiveDTO(
        UUID questEntryId,
        String objectiveKey,
        String objectiveType,
        QuestObjectiveStatus objectiveStatus,
        int progress,
        // Stage holding the objective: part of the artisan_quests_objective key. Null when read from msquests_objective.
        String stageKey
) {
    public QuestObjectiveDTO(UUID questEntryId, String objectiveKey, String objectiveType,
                             QuestObjectiveStatus objectiveStatus, int progress) {
        this(questEntryId, objectiveKey, objectiveType, objectiveStatus, progress, null);
    }
}