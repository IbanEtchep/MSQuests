package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.Quest;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor {

    protected UUID id;
    protected String name;
    protected final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

    /**
     * Constructor
     * @param id Unique ID of the actor.
     */
    public QuestActor(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getActorType();
    public abstract boolean isActor(UUID playerId);

    public Map<UUID, Quest> getQuests() {
        return Collections.unmodifiableMap(quests);
    }

    public void addQuest(Quest quest) {
        quests.put(quest.getId(), quest);
        quest.setActor(this);
    }

    public void removeQuest(Quest quest) {
        quests.remove(quest.getId());
    }
}
