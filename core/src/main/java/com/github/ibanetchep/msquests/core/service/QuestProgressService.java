package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgressService {

    private final QuestLifecycleService questLifecycleService;
    private final QuestService questService;
    private final EventDispatcher dispatcher;
    private final Map<QuestObjective, PendingObjectiveProgress> pendingProgress = new ConcurrentHashMap<>();

    public QuestProgressService(
            QuestLifecycleService questLifecycleService,
            QuestService questService,
            EventDispatcher dispatcher
    ) {
        this.questLifecycleService = questLifecycleService;
        this.questService = questService;
        this.dispatcher = dispatcher;
    }

    public void progressObjective(QuestObjective objective, int progress, @Nullable PlayerProfile profile) {
        if(objective.isCompleted()) return;

        var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
        dispatcher.dispatch(progressEvent);
        if (progressEvent.isCancelled()) return;

        var pendingObjectiveProgress = pendingProgress.compute(objective, (obj, existing) ->
                existing == null
                        ? new PendingObjectiveProgress(objective, progress, profile)
                        : new PendingObjectiveProgress(objective, existing.progress() + progress, profile)
        );

        if(objective.getProgress() + pendingObjectiveProgress.progress >= objective.getTarget()) {
            flushProgress(pendingObjectiveProgress);
        }
    }

    private CompletableFuture<Quest> flushProgress(PendingObjectiveProgress pendingObjectiveProgress) {
        QuestObjective objective = pendingObjectiveProgress.objective();
        int progress = pendingObjectiveProgress.progress();
        PlayerProfile profile = pendingObjectiveProgress.profile();

        objective.incrementProgress(progress);
        pendingProgress.remove(objective);

        var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
        dispatcher.dispatch(progressedEvent);

        if (objective.isCompleted()) {
            return questLifecycleService.completeObjective(objective, profile);
        }

        return questService.saveQuest(objective.getQuest()).thenApply(q -> objective.getQuest());
    }

    public CompletableFuture<Void> flushPendingProgress() {
        List<CompletableFuture<Quest>> futures = pendingProgress.values().stream()
                .map(this::flushProgress)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private record PendingObjectiveProgress(
            QuestObjective objective,
            int progress,
            @Nullable PlayerProfile profile
    ) {}

}
