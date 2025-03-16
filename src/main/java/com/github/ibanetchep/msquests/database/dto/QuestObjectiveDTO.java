package com.github.ibanetchep.msquests.database.dto;

import com.github.ibanetchep.msquests.model.quest.QuestObjectiveStatus;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.UUID;

public record QuestObjectiveDTO(
        @ColumnName("objective_unique_id") UUID uniqueId,
        UUID questEntryId,
        UUID objectiveId,
        int progress,
        QuestObjectiveStatus objectiveStatus,
        @ColumnName("objective_started_at") long startedAt,
        @ColumnName("objective_completed_at") long completedAt,
        @ColumnName("objective_created_at") long createdAt,
        @ColumnName("objective_updated_at") long updatedAt
) {}