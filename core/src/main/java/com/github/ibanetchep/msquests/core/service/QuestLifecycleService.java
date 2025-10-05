package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private AtomicQuestExecutor executor;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            AtomicQuestExecutor executor
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.executor = executor;
    }

    public boolean startQuest(QuestActor actor, QuestConfig questConfig) {
        Quest currentQuest = actor.getActiveQuestByKey(questConfig.getKey());

        if(currentQuest != null) {
            return false;
        }

        CoreQuestStartEvent event = new CoreQuestStartEvent(actor, questConfig);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return false;
        }

        Quest quest = questFactory.createQuest(questConfig, actor);

        CoreQuestStartedEvent startedEvent = new CoreQuestStartedEvent(quest);
        dispatcher.dispatch(startedEvent);

        executor.execute(quest, updatedQuest -> {
            questRegistry.add(updatedQuest);
            persistenceService.saveQuest(updatedQuest).join();
        });

        return true;
    }

    public <T extends QuestObjective> void progressObjective(T objective, int progress, @Nullable PlayerProfile profile) {
        var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
        dispatcher.dispatch(progressEvent);
        if (progressEvent.isCancelled()) return;

        objective.incrementProgress(progress);

        var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
        dispatcher.dispatch(progressedEvent);

        if (objective.isCompleted()) {
            completeObjective(objective, profile);
            return;
        }

        questRegistry.markDirty(objective.getQuest());
    }

    public CompletableFuture<Quest> completeObjective(QuestObjective objective, @Nullable PlayerProfile profile) {
        Quest initialQuest = objective.getQuest();

        return executor.execute(initialQuest, quest -> {
            objective.complete();
            var objectiveCompletedEvent = new CoreQuestObjectiveCompletedEvent(objective, profile);
            dispatcher.dispatch(objectiveCompletedEvent);

            if (quest.getObjectives().values().stream().allMatch(QuestObjective::isCompleted)) {
                var questCompleteEvent = new CoreQuestCompletedEvent(quest);
                dispatcher.dispatch(questCompleteEvent);
                quest.setStatus(QuestStatus.COMPLETED);
            }

            persistenceService.saveQuest(objective.getQuest()).join();
        });
    }

    public void completeQuest(Quest quest) {
        executor.execute(quest, updatedQuest -> {
            updatedQuest.setStatus(QuestStatus.COMPLETED);

            CoreQuestCompletedEvent event = new CoreQuestCompletedEvent(updatedQuest);
            dispatcher.dispatch(event);

            for (QuestAction questAction : updatedQuest.getQuestConfig().getRewards()) {
                questAction.execute(updatedQuest);
            }

            persistenceService.saveQuest(updatedQuest).join();
        });
    }
}
