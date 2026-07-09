package com.github.ibanetchep.msquests.core.repository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RotationRepository {

    CompletableFuture<Void> save(UUID actorId, String groupKey);

    CompletableFuture<Integer> countInPeriod(UUID actorId, String groupKey, Instant periodStart, Instant periodEnd);
}
