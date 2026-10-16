package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Quests in {@code artisan_quests_quest} / {@code artisan_quests_objective}, used once quests come
 * from {@code quest_group} Documents. A quest row names its config by {@code quest_uid}; the
 * {@code quest_key} / {@code group_key} columns only record the names last saved, refreshed on
 * every save.
 *
 * <p>An objective row carries {@code (quest_id, stage_key, objective_key)}. Objective keys are
 * unique within a quest, so a save matches the row on {@code (quest_id, objective_key)} and moves
 * {@code stage_key} along when the objective was moved to another stage: there is never more
 * than one row per objective, and its progress follows it.
 */
public class ArtisanQuestSqlRepository extends SqlRepository implements QuestRepository {

    public ArtisanQuestSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public CompletableFuture<Map<UUID, QuestDTO>> getAllByActor(UUID actorUniqueId) {
        String query = """
                SELECT
                    q.id as q_id, q.quest_uid as q_uid, q.quest_key as q_key, q.group_key as q_group_key,
                    q.quest_status as q_status, q.completed_at as q_completed_at,
                    q.created_at as q_created_at, q.updated_at as q_updated_at, q.actor_id as q_actor_id,
                    o.stage_key as o_stage, o.objective_key as o_key, o.objective_status as o_status,
                    o.objective_type as o_type, o.progress as o_progress
                FROM artisan_quests_quest q
                LEFT JOIN artisan_quests_objective o ON q.id = o.quest_id
                WHERE q.actor_id = :actorId
                """;

        return supplyAsync(() -> {
            Map<UUID, QuestDTO> quests = new HashMap<>();

            getJdbi().useHandle(handle -> handle.createQuery(query)
                    .bind("actorId", actorUniqueId.toString())
                    .map((rs, ctx) -> {
                        UUID questId = UUID.fromString(rs.getString("q_id"));

                        QuestDTO quest = quests.get(questId);
                        if (quest == null) {
                            Timestamp completedAt = rs.getTimestamp("q_completed_at");
                            quest = new QuestDTO(
                                    questId,
                                    rs.getString("q_key"),
                                    rs.getString("q_group_key"),
                                    UUID.fromString(rs.getString("q_actor_id")),
                                    QuestStatus.valueOf(rs.getString("q_status")),
                                    completedAt != null && completedAt.getTime() > 0 ? completedAt.getTime() : null,
                                    rs.getTimestamp("q_created_at").getTime(),
                                    rs.getTimestamp("q_updated_at").getTime(),
                                    new HashMap<>(),
                                    rs.getString("q_uid")
                            );
                            quests.put(questId, quest);
                        }

                        String objectiveKey = rs.getString("o_key");
                        if (objectiveKey != null) {
                            quest.objectives().put(objectiveKey, new QuestObjectiveDTO(
                                    questId,
                                    objectiveKey,
                                    rs.getString("o_type"),
                                    QuestObjectiveStatus.valueOf(rs.getString("o_status")),
                                    rs.getInt("o_progress"),
                                    rs.getString("o_stage")
                            ));
                        }
                        return questId;
                    })
                    .list());

            return quests;
        });
    }

    @Override
    public CompletableFuture<Void> delete(UUID questId) {
        return runAsync(() -> getJdbi().useTransaction(handle -> {
            String id = questId.toString();
            handle.createUpdate("DELETE FROM artisan_quests_objective WHERE quest_id = :questId")
                    .bind("questId", id)
                    .execute();
            handle.createUpdate("DELETE FROM artisan_quests_quest WHERE id = :id")
                    .bind("id", id)
                    .execute();
        }));
    }

    @Override
    public CompletableFuture<Void> save(QuestDTO quest) {
        return runAsync(() -> {
            if (quest.questUid() == null) {
                throw new IllegalArgumentException("Quest " + quest.id() + " (" + quest.groupKey() + "/"
                        + quest.questKey() + ") has no uid: artisan_quests_quest only stores quest_group Document quests");
            }
            getJdbi().useTransaction(handle -> {
                String questId = quest.id().toString();

                int updatedQuest = handle.createUpdate("""
                                UPDATE artisan_quests_quest
                                SET quest_status = :status, completed_at = :completedAt, quest_key = :questKey, group_key = :groupKey
                                WHERE id = :id""")
                        .bind("id", questId)
                        .bind("status", quest.status().toString())
                        .bind("completedAt", quest.completedAt() != null ? new Timestamp(quest.completedAt()) : null)
                        .bind("questKey", quest.questKey())
                        .bind("groupKey", quest.groupKey())
                        .execute();

                if (updatedQuest == 0) {
                    long now = System.currentTimeMillis();
                    handle.createUpdate("""
                                    INSERT INTO artisan_quests_quest
                                        (id, quest_uid, quest_key, group_key, quest_status, completed_at, actor_id, created_at, updated_at)
                                    VALUES (:id, :questUid, :questKey, :groupKey, :status, :completedAt, :actorId, :createdAt, :updatedAt)""")
                            .bind("id", questId)
                            .bind("questUid", quest.questUid())
                            .bind("questKey", quest.questKey())
                            .bind("groupKey", quest.groupKey())
                            .bind("status", quest.status().toString())
                            .bind("completedAt", quest.completedAt() != null ? new Timestamp(quest.completedAt()) : null)
                            .bind("actorId", quest.actorId().toString())
                            .bind("createdAt", new Timestamp(quest.createdAt() != null ? quest.createdAt() : now))
                            .bind("updatedAt", new Timestamp(quest.updatedAt() != null ? quest.updatedAt() : now))
                            .execute();
                }

                for (QuestObjectiveDTO obj : quest.objectives().values()) {
                    if (obj.stageKey() == null) {
                        throw new IllegalArgumentException("Objective " + obj.objectiveKey() + " of quest " + questId
                                + " has no stage key");
                    }
                    int updatedObj = handle.createUpdate("""
                                    UPDATE artisan_quests_objective
                                    SET objective_status = :status, progress = :progress, objective_type = :type, stage_key = :stageKey
                                    WHERE quest_id = :questId AND objective_key = :objectiveKey""")
                            .bind("questId", obj.questEntryId().toString())
                            .bind("stageKey", obj.stageKey())
                            .bind("objectiveKey", obj.objectiveKey())
                            .bind("status", obj.objectiveStatus().toString())
                            .bind("progress", obj.progress())
                            .bind("type", obj.objectiveType())
                            .execute();

                    if (updatedObj == 0) {
                        handle.createUpdate("""
                                        INSERT INTO artisan_quests_objective
                                            (quest_id, stage_key, objective_key, objective_status, objective_type, progress)
                                        VALUES (:questId, :stageKey, :objectiveKey, :status, :type, :progress)""")
                                .bind("questId", obj.questEntryId().toString())
                                .bind("stageKey", obj.stageKey())
                                .bind("objectiveKey", obj.objectiveKey())
                                .bind("status", obj.objectiveStatus().toString())
                                .bind("type", obj.objectiveType())
                                .bind("progress", obj.progress())
                                .execute();
                    }
                }
            });
        });
    }
}
