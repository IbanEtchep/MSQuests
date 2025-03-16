package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestPlayerActor extends QuestActor {

    public QuestPlayerActor(UUID id, String actorReferenceId) {
        super(id, actorReferenceId);
        this.actorReferenceId = id.toString();
    }

    @Override
    public boolean isActor(Player player) {
        return player.getUniqueId().equals(id);
    }

    @Override
    public String getActorType() {
        return "player";
    }
}
