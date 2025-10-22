package com.github.ibanetchep.msquests.core.manager;

import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgressManager {

    private final QuestLifecycleService questLifecycleService;
    private final EventDispatcher dispatcher;
    private final Map<QuestObjective, PendingObjectiveProgress> pendingProgress = new ConcurrentHashMap<>();

    public QuestProgressManager(QuestLifecycleService questLifecycleService, EventDispatcher dispatcher) {
        this.questLifecycleService = questLifecycleService;
        this.dispatcher = dispatcher;
    }

    public void progressObjective(QuestObjective objective, int progress, @Nullable PlayerProfile profile) {
        if(objective.isCompleted()) return;

        var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
        dispatcher.dispatch(progressEvent);
        if (progressEvent.isCancelled()) return;

        pendingProgress.compute(objective, (obj, existing) ->
                existing == null
                        ? new PendingObjectiveProgress(objective, progress, profile)
                        : new PendingObjectiveProgress(objective, existing.progress() + progress, profile)
        );
    }

    private CompletableFuture<Quest> flushProgress(QuestObjective objective, int progress, @Nullable PlayerProfile profile) {
        objective.incrementProgress(progress);
        var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
        dispatcher.dispatch(progressedEvent);

        if (objective.isCompleted()) {
            return questLifecycleService.completeObjective(objective, profile);
        }

        return CompletableFuture.completedFuture(objective.getQuest());
    }

    public CompletableFuture<Void> flushPendingProgress() {
        List<CompletableFuture<Quest>> futures = pendingProgress.values().stream()
                .map(p -> flushProgress(p.objective(), p.progress(), p.profile()))
                .toList();

        pendingProgress.clear();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private record PendingObjectiveProgress(
            QuestObjective objective,
            int progress,
            @Nullable PlayerProfile profile
    ) {

    }

}
