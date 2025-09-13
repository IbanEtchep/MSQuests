package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.Quest;

public class CoreQuestCompleteEvent extends CoreEvent {

    private final Quest quest;

    public CoreQuestCompleteEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

}
