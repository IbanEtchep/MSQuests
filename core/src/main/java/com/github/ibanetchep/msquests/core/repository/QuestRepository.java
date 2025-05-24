package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface QuestRepository {

    CompletableFuture<Map<UUID, QuestDTO>> getAllByActor(UUID actorUniqueId);

    CompletableFuture<Void> save(QuestDTO quest);
}