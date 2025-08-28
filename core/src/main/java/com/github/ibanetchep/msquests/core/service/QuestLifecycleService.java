package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.CoreQuestStartEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private final QuestPersistenceService loaderService;

    public QuestLifecycleService(EventDispatcher dispatcher, QuestPersistenceService loaderService) {
        this.dispatcher = dispatcher;
        this.loaderService = loaderService;
    }

    public void startQuest(QuestActor actor, QuestConfig questConfig) {
        CoreQuestStartEvent event = new CoreQuestStartEvent(actor, questConfig);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return;
        }

        Quest quest = new Quest(questConfig, actor);
        loaderService.saveQuest(quest);
    }
}
