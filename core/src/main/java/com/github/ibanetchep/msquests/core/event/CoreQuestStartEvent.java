package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

public class CoreQuestStartEvent extends CancellableCoreEvent {

    private final Quest quest;

    public CoreQuestStartEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

}
