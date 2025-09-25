package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.Quest;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AtomicQuestExecutor {

    CompletableFuture<Quest> execute(Quest quest, Consumer<Quest> action);

}
