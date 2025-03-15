package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class ActorRepository extends Repository<UUID, QuestActorDTO> {

    public ActorRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    @Nullable
    public QuestActorDTO get(UUID id) {
        String query = "SELECT * FROM actors WHERE unique_id = :uniqueId";

        return getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("uniqueId", id)
                .mapToBean(QuestActorDTO.class)
                .findFirst()
                .orElse(null));
    }

    public QuestActorDTO getByReferenceId(String type, String actorReferenceId) {
        String query = "SELECT * FROM actors WHERE actor_type = :actorType AND actor_reference_id = :actorReferenceId";

        return getJdbi().withHandle(handle ->
                handle.createQuery(query)
                        .bind("actorType", type)
                        .bind("actorReferenceId", actorReferenceId)
                        .mapToBean(QuestActorDTO.class)
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public void add(QuestActorDTO actor) {
        String query = "INSERT INTO actors (actor_type, unique_id, actor_reference_id) VALUES (:actorType, :uniqueId, :actorReferenceId)";

        getJdbi().useHandle(handle -> handle.createUpdate(query)
                .bind("actorType", actor.actorType())
                .bind("uniqueId", actor.uniqueId())
                .bind("actorReferenceId", actor.actorReferenceId())
                .execute());
    }

    @Override
    public void update(QuestActorDTO entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<UUID, QuestActorDTO> getAll() {
        String query = "SELECT * FROM actors";

        return getJdbi().withHandle(handle -> handle.createQuery(query)
                .mapToBean(QuestActorDTO.class)
                .collectToMap(QuestActorDTO::uniqueId, questActorDTO -> questActorDTO)
        );
    }

}
