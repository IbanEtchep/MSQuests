package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;

import java.util.UUID;

public interface ActorRepository {

    QuestActorDTO get(UUID id);

    void add(QuestActorDTO actor);

}
