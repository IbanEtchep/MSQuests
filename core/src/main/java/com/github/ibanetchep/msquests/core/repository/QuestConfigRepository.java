package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface QuestConfigRepository {

    CompletableFuture<Void> upsert(QuestConfigDTO dto);

    CompletableFuture<Map<String, QuestConfigDTO>> getAll();

    CompletableFuture<Void> delete(String id);
}
