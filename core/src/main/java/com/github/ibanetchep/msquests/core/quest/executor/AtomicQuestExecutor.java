package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AtomicQuestExecutor {

    CompletableFuture<Quest> execute(UUID questId, Consumer<Quest> action);

}
