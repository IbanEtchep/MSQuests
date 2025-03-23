package com.github.ibanetchep.msquests.core.scheduler;

import java.util.concurrent.CompletableFuture;

public interface Scheduler {

    CompletableFuture<Void> runAsync(ThrowingRunnable runnable);

    CompletableFuture<Void> runAsyncQueued(ThrowingRunnable runnable);

}
