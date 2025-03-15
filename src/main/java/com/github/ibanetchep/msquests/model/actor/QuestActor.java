package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class QuestActor {

    protected UUID uniqueId;
    protected String actorReferenceId;

    /**
     * Constructor
     * @param uniqueId Unique ID of the actor.
     * @param actorReferenceId Reference ID of the actor. (e.g: uuid for player, id for guild, global for global)
     */
    public QuestActor(UUID uniqueId, String actorReferenceId) {
        this.uniqueId = uniqueId;
        this.actorReferenceId = actorReferenceId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getActorReferenceId() {
        return actorReferenceId;
    }

    public abstract String getActorType();
    public abstract boolean isActor(Player player);
}
