package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;

/** A quest has just been expired, its status already set. */
public class CoreQuestExpiredEvent extends CoreEvent {

    private final Quest quest;

    public CoreQuestExpiredEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }
}
