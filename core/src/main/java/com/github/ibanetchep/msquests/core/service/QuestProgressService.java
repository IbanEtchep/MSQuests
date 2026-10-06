package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainContext;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainRunner;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgressService {

    private final QuestLifecycleService questLifecycleService;
    private final QuestService questService;
    private final EventDispatcher dispatcher;
    private final QuestRegistry questRegistry;
    private final ChainRunner chains;

    /**
     * Batched progress, keyed by identity of the objective rather than by instance:
     * a reconnect rebuilds the whole Quest graph, and a batch pinned to the previous
     * instance would be written to an object no longer reachable from the registry.
     */
    private final Map<ObjectiveKey, PendingObjectiveProgress> pendingProgress = new ConcurrentHashMap<>();

    public QuestProgressService(
            QuestLifecycleService questLifecycleService,
            QuestService questService,
            EventDispatcher dispatcher,
            QuestRegistry questRegistry,
            ChainRunner chains
    ) {
        this.questLifecycleService = questLifecycleService;
        this.questService = questService;
        this.dispatcher = dispatcher;
        this.questRegistry = questRegistry;
        this.chains = chains;
    }

    public void progressObjective(QuestObjective objective, int progress, @Nullable PlayerProfile profile) {
        if(objective.isCompleted()) return;

        var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
        dispatcher.dispatch(progressEvent);
        if (progressEvent.isCancelled()) return;

        ObjectiveKey key = ObjectiveKey.of(objective);

        var pendingObjectiveProgress = pendingProgress.compute(key, (k, existing) ->
                existing == null
                        ? new PendingObjectiveProgress(k, progress, profile)
                        : new PendingObjectiveProgress(k, existing.progress() + progress, profile)
        );

        if(objective.getProgress() + pendingObjectiveProgress.progress >= objective.getTarget()) {
            flushProgress(pendingObjectiveProgress);
        }
    }

    /**
     * Progress that must not sit in the batch: what a player hands over (items, money) is
     * already gone from them, so it is persisted right away, and the next contribution reads
     * an up-to-date {@code getProgress()} to know what is still missing.
     */
    public CompletableFuture<Quest> progressObjectiveNow(QuestObjective objective, int progress, @Nullable PlayerProfile profile) {
        progressObjective(objective, progress, profile);
        PendingObjectiveProgress pending = pendingProgress.get(ObjectiveKey.of(objective));
        return pending != null
                ? flushProgress(pending)
                : CompletableFuture.completedFuture(objective.getQuest());
    }

    private CompletableFuture<Quest> flushProgress(PendingObjectiveProgress pendingObjectiveProgress) {
        ObjectiveKey key = pendingObjectiveProgress.key();
        pendingProgress.remove(key);

        QuestObjective objective = resolve(key);
        if (objective == null || objective.isCompleted()) {
            // Quest deleted, rotated away or already finished elsewhere: nothing to write.
            return CompletableFuture.completedFuture(null);
        }

        PlayerProfile profile = pendingObjectiveProgress.profile();
        objective.incrementProgress(pendingObjectiveProgress.progress());

        var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
        dispatcher.dispatch(progressedEvent);

        chains.run(objective.getQuest().getQuestGroup().getObjectiveProgressActions(), ChainContext.of(objective));

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

    /**
     * Re-reads the objective from the registry so the batch is applied to whatever
     * instance is live now, and returns null once the quest is gone.
     */
    private @Nullable QuestObjective resolve(ObjectiveKey key) {
        Quest quest = questRegistry.getQuest(key.questId());
        if (quest == null) {
            return null;
        }

        for (QuestStage stage : quest.getStages().values()) {
            QuestObjective objective = stage.getObjectives().get(key.objectiveKey());
            if (objective != null) {
                return objective;
            }
        }

        return null;
    }

    /** Mirrors the composite primary key objectives are persisted under. */
    private record ObjectiveKey(UUID questId, String objectiveKey) {
        static ObjectiveKey of(QuestObjective objective) {
            return new ObjectiveKey(objective.getQuest().getId(), objective.getObjectiveConfig().getKey());
        }
    }

    private record PendingObjectiveProgress(
            ObjectiveKey key,
            int progress,
            @Nullable PlayerProfile profile
    ) {}

}
