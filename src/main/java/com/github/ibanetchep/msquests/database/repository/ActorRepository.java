package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ActorRepository extends Repository {

    public ActorRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Nullable
    public QuestActorDTO get(UUID id) {
        String query = "SELECT * FROM msquests_actor WHERE id = :id";

        return getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("id", id)
                .mapToBean(QuestActorDTO.class)
                .findFirst()
                .orElse(null));
    }

    public QuestActorDTO getByReferenceId(String type, String actorReferenceId) {
        String query = """
        SELECT id, actor_type, actor_reference_id
        FROM msquests_actor WHERE actor_type = :actorType AND actor_reference_id = :actorReferenceId
        """;

        return getJdbi().withHandle(handle ->
                handle.createQuery(query)
                        .bind("actorType", type)
                        .bind("actorReferenceId", actorReferenceId)
                        .mapTo(QuestActorDTO.class)
                        .findFirst()
                        .orElse(null));
    }

    public void add(QuestActorDTO actor) {
        String query = "INSERT INTO msquests_actor (actor_type, id, actor_reference_id) VALUES (:actorType, :id, :actorReferenceId)";

        getJdbi().useHandle(handle -> handle.createUpdate(query)
                .bind("actorType", actor.actorType())
                .bind("id", actor.id().toString())
                .bind("actorReferenceId", actor.actorReferenceId())
                .execute());
    }

    public void update(QuestActorDTO entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
