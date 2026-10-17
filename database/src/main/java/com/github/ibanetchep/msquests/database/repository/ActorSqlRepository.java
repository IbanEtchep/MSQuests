package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.SqlTables;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ActorSqlRepository extends SqlRepository implements ActorRepository {

    private final SqlTables tables;

    public ActorSqlRepository(DbAccess dbAccess) {
        this(dbAccess, SqlTables.LEGACY);
    }

    public ActorSqlRepository(DbAccess dbAccess, SqlTables tables) {
        super(dbAccess);
        this.tables = tables;
    }

    @Override
    public CompletableFuture<QuestActorDTO> get(UUID id) {
        String query = "SELECT * FROM " + tables.actor() + " WHERE id = :id";

        return supplyAsync(() -> getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("id", id.toString())
                .mapTo(QuestActorDTO.class)
                .findFirst()
                .orElse(null)));
    }

    @Override
    public CompletableFuture<Void> add(QuestActorDTO actor) {
        String query = "INSERT INTO " + tables.actor() + " (id, actor_type) VALUES (:id, :actorType)";

        return runAsync(() -> getJdbi().useHandle(handle -> handle.createUpdate(query)
                .bind("actorType", actor.actorType())
                .bind("id", actor.id().toString())
                .execute()));
    }
}
