package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AtomicLocalQuestExecutor implements AtomicQuestExecutor {

    private final QuestRegistry questRegistry;
    private final Map<UUID, ExecutorService> executors = new ConcurrentHashMap<>();

    public AtomicLocalQuestExecutor(QuestRegistry questRegistry) {
        this.questRegistry = questRegistry;
    }

    @Override
    public CompletableFuture<Quest> execute(UUID questId, Consumer<Quest> action) {
        ExecutorService questExecutor = executors.computeIfAbsent(questId, id -> Executors.newSingleThreadExecutor());

        return CompletableFuture.supplyAsync(() -> {
            Quest quest = questRegistry.getQuest(questId);
            action.accept(quest);
            return quest;
        }, questExecutor);
    }

}
