package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QuestConfigYamlRepository implements QuestConfigRepository {
    @Override
    public CompletableFuture<Void> upsert(QuestConfigDTO dto) {
        return null;
    }

    @Override
    public CompletableFuture<Map<UUID, QuestConfigDTO>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<Void> delete(UUID id) {
        return null;
    }
}
