package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.Quest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor {

    protected UUID id;
    protected final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

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

    public Map<UUID, Quest> getQuests() {
        return quests;
    }
}
