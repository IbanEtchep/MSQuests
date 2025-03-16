package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class QuestActor {

    protected UUID id;
    protected String actorReferenceId;

    /**
     * Constructor
     * @param id Unique ID of the actor.
     * @param actorReferenceId Reference ID of the actor. (e.g: uuid for player, id for guild, global for global)
     */
    public QuestActor(UUID id, String actorReferenceId) {
        this.id = id;
        this.actorReferenceId = actorReferenceId;
    }

    public UUID getId() {
        return id;
    }

    public String getActorReferenceId() {
        return actorReferenceId;
    }

    public abstract String getActorType();
    public abstract boolean isActor(Player player);
}
