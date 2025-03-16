package com.github.ibanetchep.msquests.database.dto;

import com.github.ibanetchep.msquests.model.quest.QuestObjectiveStatus;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.UUID;

public record QuestObjectiveDTO(
        UUID id,
        UUID questEntryId,
        UUID objectiveId,
        int progress,
        QuestObjectiveStatus objectiveStatus,
        long startedAt,
        long completedAt,
        long createdAt,
        long updatedAt
) {}