package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicObjectiveExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.cache.QuestCache;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private AtomicQuestExecutor executor;
    private AtomicObjectiveExecutor objectiveExecutor;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;
    private final QuestCache questCache;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestCache questCache,
            AtomicQuestExecutor executor,
            AtomicObjectiveExecutor objectiveExecutor
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questCache = questCache;
        this.executor = executor;
        this.objectiveExecutor = objectiveExecutor;
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
            questCache.add(updatedQuest);
            persistenceService.saveQuest(updatedQuest).join();
        });

        return true;
    }

    public <T extends QuestObjective> void updateObjectiveProgress(T objective, Consumer<T> progressAction, @Nullable PlayerProfile profile) {
        objectiveExecutor.execute(objective, updatedObjective -> {
            Quest quest = updatedObjective.getQuest();

            var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
            dispatcher.dispatch(progressEvent);
            if (progressEvent.isCancelled()) return;

            progressAction.accept(updatedObjective);

            var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
            dispatcher.dispatch(progressedEvent);

            if (objective.isCompleted()) {
                var objectiveCompleteEvent = new CoreQuestObjectiveCompleteEvent(objective, profile);
                dispatcher.dispatch(objectiveCompleteEvent);

                if (quest.getObjectives().values().stream().allMatch(QuestObjective::isCompleted)) {
                    var questCompleteEvent = new CoreQuestCompletedEvent(quest);
                    dispatcher.dispatch(questCompleteEvent);
                    quest.setStatus(QuestStatus.COMPLETED);
                }

                persistenceService.saveQuest(objective.getQuest()).join();
                return;
            }

            questCache.markDirty(objective.getQuest());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
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
