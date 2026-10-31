package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;

/** A quest has just been abandoned, its status already set. */
public class CoreQuestAbandonedEvent extends CoreEvent {

    private final Quest quest;

    public CoreQuestAbandonedEvent(Quest quest) {
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }
}
