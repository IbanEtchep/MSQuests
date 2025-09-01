package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.CoreQuestStartEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private final QuestPersistenceService persistenceService;

    public QuestLifecycleService(EventDispatcher dispatcher, QuestPersistenceService persistenceService) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
    }

    public boolean startQuest(QuestActor actor, QuestConfig questConfig) {
        CoreQuestStartEvent event = new CoreQuestStartEvent(actor, questConfig);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return false;
        }

        Quest quest = new Quest(questConfig, actor);
        persistenceService.saveQuest(quest);

        return true;
    }

    public <T extends QuestObjective<?>> void updateObjectiveProgress(T objective, int amount) {
        int newProgress = objective.getProgress() + amount;
        int target = objective.getObjectiveConfig().getTargetAmount();
        objective.setProgress(Math.min(newProgress, target));

        if (objective.isCompleted()) {
            objective.setStatus(QuestObjectiveStatus.COMPLETED);
            //TODO dispatch objective complete event

            Quest quest = objective.getQuest();
            if (quest.getObjectives().values().stream().allMatch(QuestObjective::isCompleted)) {
                //TODO complete quest
            }
        }

        persistenceService.saveQuest(objective.getQuest());
    }
}
