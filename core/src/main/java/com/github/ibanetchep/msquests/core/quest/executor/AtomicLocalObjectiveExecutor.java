package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AtomicLocalObjectiveExecutor implements AtomicObjectiveExecutor {

    private final AtomicQuestExecutor questExecutor;

    public AtomicLocalObjectiveExecutor(AtomicQuestExecutor questExecutor) {
        this.questExecutor = questExecutor;
    }

    @Override
    public <T extends QuestObjective> CompletableFuture<T> execute(T objective, Consumer<T> action) {
        return questExecutor
                .execute(objective.getQuest(), quest -> action.accept(objective))
                .thenApply(quest -> objective);
    }
}