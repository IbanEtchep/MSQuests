package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ActorRepository {

    CompletableFuture<QuestActorDTO> get(UUID id);

    CompletableFuture<Void> add(QuestActorDTO actor);

}
