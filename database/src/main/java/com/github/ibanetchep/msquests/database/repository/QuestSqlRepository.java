package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QuestSqlRepository extends SqlRepository implements QuestRepository {

    public QuestSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public CompletableFuture<Map<UUID, QuestDTO>> getAllByActor(UUID actorUniqueId) {
        String query = """
                SELECT 
                    q.id as q_id, q.quest_key as q_key, q.quest_group_key as q_group_key,
                    q.quest_status as q_status, 
                    q.started_at as q_started_at, q.completed_at as q_completed_at, 
                    q.expires_at as q_expires_at, q.created_at as q_created_at, 
                    q.updated_at as q_updated_at, q.actor_id as q_actor_id,
                    o.objective_key as o_key, o.objective_status as o_status, 
                    o.progress as o_progress, o.quest_id as o_quest_id
                FROM msquests_quest q
                LEFT JOIN msquests_objective o ON q.id = o.quest_id
                WHERE q.actor_id = :actorId
                """;

        return supplyAsync(() -> {
            Map<UUID, QuestDTO> quests = new HashMap<>();

            getJdbi().useHandle(handle -> {
                handle.createQuery(query)
                        .bind("actorId", actorUniqueId.toString())
                        .map((rs, ctx) -> {
                            UUID questId = UUID.fromString(rs.getString("q_id"));

                            if (!quests.containsKey(questId)) {
                                String questKey = rs.getString("q_key");
                                String groupKey = rs.getString("q_group_key");
                                UUID actorId = UUID.fromString(rs.getString("q_actor_id"));
                                QuestStatus status = QuestStatus.valueOf(rs.getString("q_status"));

                                long completedAt = rs.getTimestamp("q_completed_at") != null ? rs.getTimestamp("q_completed_at").getTime() : 0;
                                long createdAt = rs.getTimestamp("q_created_at").getTime();
                                long updatedAt = rs.getTimestamp("q_updated_at").getTime();

                                QuestDTO quest = new QuestDTO(
                                        questId,
                                        questKey,
                                        groupKey,
                                        actorId,
                                        status,
                                        completedAt,
                                        createdAt,
                                        updatedAt,
                                        new HashMap<>()  // map mutable ici
                                );

                                quests.put(questId, quest);
                            }

                            String objKey = rs.getString("o_key");
                            if (objKey != null) {
                                QuestObjectiveStatus objStatus = QuestObjectiveStatus.valueOf(rs.getString("o_status"));
                                int progress = rs.getInt("o_progress");

                                QuestObjectiveDTO objective = new QuestObjectiveDTO(
                                        questId,
                                        objKey,
                                        progress,
                                        objStatus
                                );

                                quests.get(questId).objectives().put(objKey, objective);
                            }

                            return questId;
                        })
                        .list();
            });

            return quests;
        });
    }

    @Override
    public CompletableFuture<Void> save(QuestDTO quest) {
        return runAsync(() -> getJdbi().useTransaction(handle -> {
            String questId = quest.id().toString();

            int updatedQuest = handle.createUpdate(
                            "UPDATE msquests_quest SET quest_status = :status, completed_at = :completedAt WHERE id = :id")
                    .bind("id", questId)
                    .bind("status", quest.status().toString())
                    .bind("completedAt", quest.completedAt() != null ? new Timestamp(quest.completedAt()) : null)
                    .execute();

            if (updatedQuest == 0) {
                handle.createUpdate(
                                "INSERT INTO msquests_quest (id, quest_key, quest_group_key, quest_status, actor_id) " +
                                        "VALUES (:id, :questKey, :groupKey, :status, :actorId)")
                        .bind("id", questId)
                        .bind("questKey", quest.questKey())
                        .bind("groupKey", quest.groupKey())
                        .bind("status", quest.status().toString())
                        .bind("actorId", quest.actorId().toString())
                        .execute();
            }

            if (quest.objectives() != null) {
                for (QuestObjectiveDTO obj : quest.objectives().values()) {
                    String objectiveKey = obj.objectiveKey();
                    String questEntryId = obj.questEntryId().toString();

                    int updatedObj = handle.createUpdate(
                                    "UPDATE msquests_objective SET objective_status = :status, progress = :progress " +
                                            "WHERE quest_id = :questId AND objective_key = :objectiveKey")
                            .bind("objectiveKey", objectiveKey)
                            .bind("status", obj.objectiveStatus().toString())
                            .bind("progress", obj.progress())
                            .bind("questId", questEntryId)
                            .execute();

                    if (updatedObj == 0) {
                        handle.createUpdate(
                                        "INSERT INTO msquests_objective (objective_key, objective_status, progress, quest_id) " +
                                                "VALUES (:objectiveKey, :status, :progress, :questId)")
                                .bind("objectiveKey", objectiveKey)
                                .bind("status", obj.objectiveStatus().toString())
                                .bind("progress", obj.progress())
                                .bind("questId", questEntryId)
                                .execute();
                    }
                }
            }
        }));
    }

}