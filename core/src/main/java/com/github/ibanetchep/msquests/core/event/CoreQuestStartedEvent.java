package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;

public class CoreQuestStartedEvent extends CoreEvent {

    private final Quest quest;

    public CoreQuestStartedEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

}
