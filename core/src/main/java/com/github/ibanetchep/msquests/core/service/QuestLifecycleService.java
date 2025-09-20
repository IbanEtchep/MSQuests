package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.CoreQuestCompleteEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveCompleteEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestStartEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.*;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
    }

    public boolean startQuest(QuestActor actor, QuestConfig questConfig) {
        Quest currentQuest = actor.getActiveQuestByKey(questConfig.getKey());

        if(currentQuest != null) {
            return false;
        }

        Quest quest = questFactory.createQuest(questConfig, actor);

        CoreQuestStartEvent event = new CoreQuestStartEvent(quest);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return false;
        }

        actor.addQuest(quest);
        persistenceService.saveQuest(quest);

        return true;
    }

    public void updateObjectiveProgress(QuestObjective<?> objective, int amount) {
        int newProgress = objective.getProgress() + amount;
        int target = objective.getObjectiveConfig().getTargetAmount();
        objective.setProgress(Math.min(newProgress, target));

        if (objective.isCompleted()) {
            objective.setStatus(QuestObjectiveStatus.COMPLETED);
            CoreQuestObjectiveCompleteEvent objectiveCompleteEvent = new CoreQuestObjectiveCompleteEvent(objective);
            dispatcher.dispatch(objectiveCompleteEvent);

            Quest quest = objective.getQuest();
            if (quest.getObjectives().values().stream().allMatch(QuestObjective::isCompleted)) {
                CoreQuestCompleteEvent questCompleteEvent = new CoreQuestCompleteEvent(quest);
                dispatcher.dispatch(questCompleteEvent);
                quest.setStatus(QuestStatus.COMPLETED);
            }
        }

        persistenceService.saveQuest(objective.getQuest());
    }

    public void completeQuest(Quest quest) {
        for (QuestObjective<?> objective : quest.getObjectives().values()) {
            objective.setStatus(QuestObjectiveStatus.COMPLETED);
        }

        CoreQuestCompleteEvent event = new CoreQuestCompleteEvent(quest);
        dispatcher.dispatch(event);

        for (QuestAction questAction : quest.getQuestConfig().getRewards()) {
            questAction.execute(quest);
        }

        quest.setStatus(QuestStatus.COMPLETED);
        persistenceService.saveQuest(quest);
    }
}
