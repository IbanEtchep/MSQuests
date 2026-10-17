package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record QuestDTO(
        UUID id,
        String questKey,
        String groupKey,
        UUID actorId,
        QuestStatus status,
        Long completedAt,
        Long createdAt,
        Long updatedAt,
        Map<String, QuestObjectiveDTO> objectives,
        // The quest config's uid: what artisan_quests_quest stores. Null for quests/*.yml configs.
        String questUid
) {
    public QuestDTO(UUID id, String questKey, String groupKey, UUID actorId, QuestStatus status, Long completedAt,
                    Long createdAt, Long updatedAt, Map<String, QuestObjectiveDTO> objectives) {
        this(id, questKey, groupKey, actorId, status, completedAt, createdAt, updatedAt, objectives, null);
    }

    public QuestDTO {
        objectives = objectives == null ? new HashMap<>() : objectives;
    }
}