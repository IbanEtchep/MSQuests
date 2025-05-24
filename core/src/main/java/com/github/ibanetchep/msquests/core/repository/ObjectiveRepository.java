package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ObjectiveRepository {

    CompletableFuture<Map<UUID, QuestObjectiveDTO>> getAllByQuest(UUID questId);

    CompletableFuture<Void> save(QuestObjectiveDTO objective);
}