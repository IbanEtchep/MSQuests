package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.repository.ObjectiveRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ObjectiveSqlRepository extends SqlRepository implements ObjectiveRepository {

    public ObjectiveSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public CompletableFuture<Map<UUID, QuestObjectiveDTO>> getAllByQuest(UUID questId) {
        String query = """
                SELECT o.*
                FROM msquests_objective o
                WHERE o.quest_id = :questId
                """;

        return supplyAsync(() -> getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("questId", questId.toString())
                .mapTo(QuestObjectiveDTO.class)
                .stream()
                .collect(Collectors.toMap(QuestObjectiveDTO::id, objective -> objective))));
    }

    @Override
    public CompletableFuture<Void> save(QuestObjectiveDTO objective) {
        String query = """
                INSERT INTO msquests_objective (id, objective_key, objective_status, progress, completed_at, quest_id, created_at, updated_at) 
                VALUES (:id, :objectiveKey, :objectiveStatus, :progress, :completedAt, :questId, :createdAt, :updatedAt);
                """;

        String updateQuery = """
                UPDATE msquests_objective 
                SET objective_status = :objectiveStatus,
                    progress = :progress,
                    completed_at = :completedAt,
                    updated_at = :updatedAt
                WHERE id = :id;
                """;

        return runAsync(() -> getJdbi().useTransaction(handle -> {
            // Préparer les timestamps
            long now = System.currentTimeMillis();
            Timestamp currentTime = new Timestamp(now);

            // Try update first
            int updated = handle.createUpdate(updateQuery)
                    .bind("id", objective.id().toString())
                    .bind("objectiveStatus", objective.objectiveStatus().toString())
                    .bind("progress", objective.progress())
                    .bind("completedAt", objective.completedAt() > 0 ? new Timestamp(objective.completedAt()) : null)
                    .bind("updatedAt", currentTime)
                    .execute();

            // If no rows were updated, insert
            if (updated == 0) {
                handle.createUpdate(query)
                        .bind("id", objective.id().toString())
                        .bind("objectiveKey", objective.objectiveKey())
                        .bind("objectiveStatus", objective.objectiveStatus().toString())
                        .bind("progress", objective.progress())
                        .bind("completedAt", objective.completedAt() > 0 ? new Timestamp(objective.completedAt()) : null)
                        .bind("questId", objective.questEntryId().toString())
                        .bind("createdAt", objective.createdAt() > 0 ? new Timestamp(objective.createdAt()) : currentTime)
                        .bind("updatedAt", objective.updatedAt() > 0 ? new Timestamp(objective.updatedAt()) : currentTime)
                        .execute();
            }
        }));
    }
}