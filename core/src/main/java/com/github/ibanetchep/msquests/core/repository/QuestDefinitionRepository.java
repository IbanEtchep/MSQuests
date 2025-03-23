package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDefinitionDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface QuestDefinitionRepository {

    CompletableFuture<Void> upsert(QuestDefinitionDTO dto);

    CompletableFuture<Map<UUID, QuestDefinitionDTO>> getAll();

    CompletableFuture<Void> delete(UUID id);
}
