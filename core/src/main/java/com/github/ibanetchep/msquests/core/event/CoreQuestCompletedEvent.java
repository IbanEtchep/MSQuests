package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.Quest;

public class CoreQuestCompletedEvent extends CoreEvent {

    private final Quest quest;

    public CoreQuestCompletedEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

}
