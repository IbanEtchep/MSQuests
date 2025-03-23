package com.github.ibanetchep.msquests.core.quest.actor;

import java.util.UUID;

public abstract class QuestActor {

    protected UUID id;

    /**
     * Constructor
     * @param id Unique ID of the actor.
     */
    public QuestActor(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public abstract String getActorType();
    public abstract boolean isActor(UUID playerId);
}
