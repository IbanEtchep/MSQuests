package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.jetbrains.annotations.Nullable;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestRegistry questRegistry
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
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

        questRegistry.registerQuest(quest);
        persistenceService.saveQuest(quest);

        return true;
    }

    public void updateObjectiveProgress(QuestObjective objective, Runnable progressAction, @Nullable PlayerProfile profile) {
        var progressEvent = new CoreQuestObjectiveProgressEvent(objective, profile);
        dispatcher.dispatch(progressEvent);
        if (progressEvent.isCancelled()) return;

        progressAction.run();

        var progressedEvent = new CoreQuestObjectiveProgressedEvent(objective, profile);
        dispatcher.dispatch(progressedEvent);

        if (objective.isCompleted()) {
            var objectiveCompleteEvent = new CoreQuestObjectiveCompleteEvent(objective, profile);
            dispatcher.dispatch(objectiveCompleteEvent);

            Quest quest = objective.getQuest();
            if (quest.getObjectives().values().stream().allMatch(QuestObjective::isCompleted)) {
                var questCompleteEvent = new CoreQuestCompletedEvent(quest);
                dispatcher.dispatch(questCompleteEvent);
                quest.setStatus(QuestStatus.COMPLETED);
            }

            persistenceService.saveQuest(objective.getQuest());
            return;
        }

        questRegistry.markDirty(objective.getQuest());
    }


    public void completeQuest(Quest quest) {
        quest.setStatus(QuestStatus.COMPLETED);

        CoreQuestCompletedEvent event = new CoreQuestCompletedEvent(quest);
        dispatcher.dispatch(event);

        for (QuestAction questAction : quest.getQuestConfig().getRewards()) {
            questAction.execute(quest);
        }

        persistenceService.saveQuest(quest);
    }
}
