package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.SqlTables;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RotationSqlRepository extends SqlRepository implements RotationRepository {

    private final SqlTables tables;

    public RotationSqlRepository(DbAccess dbAccess) {
        this(dbAccess, SqlTables.LEGACY);
    }

    public RotationSqlRepository(DbAccess dbAccess, SqlTables tables) {
        super(dbAccess);
        this.tables = tables;
    }

    @Override
    public CompletableFuture<Void> save(UUID actorId, String groupId) {
        return runAsync(() -> getJdbi().useHandle(handle ->
                handle.createUpdate(
                        "INSERT INTO " + tables.rotation() + " (id, actor_id, " + tables.rotationGroupColumn()
                                + ") VALUES (:id, :actorId, :groupId)")
                        .bind("id", UUID.randomUUID().toString())
                        .bind("actorId", actorId.toString())
                        .bind("groupId", groupId)
                        .execute()
        ));
    }

    @Override
    public CompletableFuture<Integer> countInPeriod(UUID actorId, String groupId, Instant periodStart, Instant periodEnd) {
        return supplyAsync(() -> getJdbi().withHandle(handle -> {
            String query = "SELECT COUNT(*) FROM " + tables.rotation() + " WHERE actor_id = :actorId AND "
                    + tables.rotationGroupColumn() + " = :groupId";

            if (periodStart != null) {
                query += " AND rotated_at >= :periodStart";
            }
            if (periodEnd != null) {
                query += " AND rotated_at < :periodEnd";
            }

            var q = handle.createQuery(query)
                    .bind("actorId", actorId.toString())
                    .bind("groupId", groupId);

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
