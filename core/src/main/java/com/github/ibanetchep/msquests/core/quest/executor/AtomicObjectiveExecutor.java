package com.github.ibanetchep.msquests.core.quest.executor;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AtomicObjectiveExecutor {

    <T extends QuestObjective> CompletableFuture<T> execute(T objective, Consumer<T> action);

}
