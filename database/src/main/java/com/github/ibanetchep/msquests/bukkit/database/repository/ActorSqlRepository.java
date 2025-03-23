package com.github.ibanetchep.msquests.bukkit.database.repository;

import com.github.ibanetchep.msquests.bukkit.database.DbAccess;
import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ActorSqlRepository extends SqlRepository implements ActorRepository {

    public ActorSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public CompletableFuture<QuestActorDTO> get(UUID id) {
        String query = "SELECT * FROM msquests_actor WHERE id = :id";

        return supplyAsync(() -> getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("id", id)
                .mapTo(QuestActorDTO.class)
                .findFirst()
                .orElse(null)));
    }

    @Override
    public CompletableFuture<Void> add(QuestActorDTO actor) {
        String query = "INSERT INTO msquests_actor (id, actor_type) VALUES (:id, :actorType)";

        return runAsync(() -> getJdbi().useHandle(handle -> handle.createUpdate(query)
                .bind("actorType", actor.actorType())
                .bind("id", actor.id().toString())
                .execute()));
    }
}
