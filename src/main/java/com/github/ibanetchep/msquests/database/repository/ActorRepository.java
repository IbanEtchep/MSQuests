package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;

public class ActorRepository extends Repository {

    public ActorRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    public QuestActorDTO getByReferenceId(String type, String actorReferenceId) {
        return null;
    }

    public void save(QuestActorDTO actor) {

    }

}
