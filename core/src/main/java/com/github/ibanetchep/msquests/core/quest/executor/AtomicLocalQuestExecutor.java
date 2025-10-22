package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class AtomicLocalQuestExecutor implements AtomicQuestExecutor {

    private final QuestRegistry questRegistry;
    private final ExecutorService executor;
    private final Map<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    public AtomicLocalQuestExecutor(QuestRegistry questRegistry) {
        this.questRegistry = questRegistry;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<Quest> execute(UUID questId, Consumer<Quest> action) {
        ReentrantLock lock = locks.computeIfAbsent(questId, id -> new ReentrantLock());

        return CompletableFuture.supplyAsync(() -> {
            lock.lock();
            try {
                Quest quest = questRegistry.getQuest(questId);
                action.accept(quest);
                return quest;
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}