package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
                    o.id as o_id, o.objective_key as o_key, o.objective_status as o_status, 
                    o.progress as o_progress, o.started_at as o_started_at, 
                    o.completed_at as o_completed_at, o.quest_id as o_quest_id,
                    o.created_at as o_created_at, o.updated_at as o_updated_at
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

                            String objIdStr = rs.getString("o_id");
                            if (objIdStr != null) {
                                UUID objId = UUID.fromString(objIdStr);
                                String objKey = rs.getString("o_key");
                                QuestObjectiveStatus objStatus = QuestObjectiveStatus.valueOf(rs.getString("o_status"));
                                int progress = rs.getInt("o_progress");

                                long objCompletedAt = rs.getTimestamp("o_completed_at") != null ? rs.getTimestamp("o_completed_at").getTime() : 0;

                                long objCreatedAt = rs.getTimestamp("o_created_at").getTime();
                                long objUpdatedAt = rs.getTimestamp("o_updated_at").getTime();

                                QuestObjectiveDTO objective = new QuestObjectiveDTO(
                                        objId,
                                        questId,
                                        objKey,
                                        progress,
                                        objStatus,
                                        objCompletedAt,
                                        objCreatedAt,
                                        objUpdatedAt
                                );

                                quests.get(questId).objectives().put(objId, objective);
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
        String questUpsertQuery = """
                INSERT INTO msquests_quest (id, quest_key, quest_group_key, quest_status, completed_at, actor_id, created_at, updated_at) 
                VALUES (:id, :questKey, :groupKey, :status, :completedAt, :actorId, :createdAt, :updatedAt)
                ON DUPLICATE KEY UPDATE
                    quest_status = :status,
                    started_at = :startedAt,
                    expires_at = :expiresAt,
                    completed_at = :completedAt,
                    updated_at = :updatedAt;
                """;

        String objectiveUpsertQuery = """
                INSERT INTO msquests_objective (id, objective_key, objective_status, progress, completed_at, quest_id, created_at, updated_at) 
                VALUES (:id, :objectiveKey, :status, :progress, :completedAt, :questId, :createdAt, :updatedAt)
                ON DUPLICATE KEY UPDATE 
                    objective_status = :status,
                    progress = :progress,
                    started_at = :startedAt,
                    completed_at = :completedAt,
                    updated_at = :updatedAt;
                """;

        return runAsync(() -> getJdbi().useTransaction(handle -> {
            long now = System.currentTimeMillis();
            Timestamp currentTime = new Timestamp(now);

            handle.createUpdate(questUpsertQuery)
                    .bind("id", quest.id().toString())
                    .bind("questKey", quest.questKey())
                    .bind("groupKey", quest.groupKey())
                    .bind("status", quest.status().toString())
                    .bind("completedAt", quest.completedAt() > 0 ? new Timestamp(quest.completedAt()) : null)
                    .bind("actorId", quest.actorId().toString())
                    .bind("createdAt", quest.createdAt() > 0 ? new Timestamp(quest.createdAt()) : currentTime)
                    .bind("updatedAt", quest.updatedAt() > 0 ? new Timestamp(quest.updatedAt()) : currentTime)
                    .execute();

            if (quest.objectives() != null && !quest.objectives().isEmpty()) {
                var objectiveBatch = handle.prepareBatch(objectiveUpsertQuery);

                for (QuestObjectiveDTO objective : quest.objectives().values()) {
                    objectiveBatch
                            .bind("id", objective.id().toString())
                            .bind("objectiveKey", objective.objectiveKey())
                            .bind("status", objective.objectiveStatus().toString())
                            .bind("progress", objective.progress())
                            .bind("completedAt", objective.completedAt() > 0 ? new Timestamp(objective.completedAt()) : null)
                            .bind("questId", objective.questEntryId().toString())
                            .bind("createdAt", objective.createdAt() > 0 ? new Timestamp(objective.createdAt()) : currentTime)
                            .bind("updatedAt", objective.updatedAt() > 0 ? new Timestamp(objective.updatedAt()) : currentTime)
                            .add();
                }

                objectiveBatch.execute();
            }
        }));
    }
}