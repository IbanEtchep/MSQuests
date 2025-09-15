package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

public class CoreQuestStartEvent extends CancellableCoreEvent {

    private final QuestActor actor;
    private final QuestConfig questConfig;

    public CoreQuestStartEvent(QuestActor actor, QuestConfig questConfig) {
        this.actor = actor;
        this.questConfig = questConfig;
    }

    public QuestActor getActor() {
        return actor;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

}
