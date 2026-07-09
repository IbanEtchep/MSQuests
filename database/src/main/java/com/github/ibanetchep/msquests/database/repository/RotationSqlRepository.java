package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RotationSqlRepository extends SqlRepository implements RotationRepository {

    public RotationSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public CompletableFuture<Void> save(UUID actorId, String groupKey) {
        return runAsync(() -> getJdbi().useHandle(handle ->
                handle.createUpdate(
                        "INSERT INTO msquests_rotation (id, actor_id, group_key) VALUES (:id, :actorId, :groupKey)")
                        .bind("id", UUID.randomUUID().toString())
                        .bind("actorId", actorId.toString())
                        .bind("groupKey", groupKey)
                        .execute()
        ));
    }

    @Override
    public CompletableFuture<Integer> countInPeriod(UUID actorId, String groupKey, Instant periodStart, Instant periodEnd) {
        return supplyAsync(() -> getJdbi().withHandle(handle -> {
            String query = "SELECT COUNT(*) FROM msquests_rotation WHERE actor_id = :actorId AND group_key = :groupKey";

            if (periodStart != null) {
                query += " AND rotated_at >= :periodStart";
            }
            if (periodEnd != null) {
                query += " AND rotated_at < :periodEnd";
            }

            var q = handle.createQuery(query)
                    .bind("actorId", actorId.toString())
                    .bind("groupKey", groupKey);

            if (periodStart != null) {
                q.bind("periodStart", Timestamp.from(periodStart));
            }
            if (periodEnd != null) {
                q.bind("periodEnd", Timestamp.from(periodEnd));
            }

            return q.mapTo(Integer.class).one();
        }));
    }
}
