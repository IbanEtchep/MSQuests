package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.Quest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AtomicLocalQuestExecutor implements AtomicQuestExecutor {

    private final Map<UUID, ExecutorService> executors = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Quest> execute(Quest quest, Consumer<Quest> action) {
        ExecutorService questExecutor = executors.computeIfAbsent(quest.getId(), id -> Executors.newSingleThreadExecutor());

        return CompletableFuture.supplyAsync(() -> {
            action.accept(quest);
            return quest;
        }, questExecutor);
    }

}
