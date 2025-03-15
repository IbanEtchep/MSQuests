package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestPlayerActor extends QuestActor {

    public QuestPlayerActor(UUID uniqueId, String actorReferenceId) {
        super(uniqueId, actorReferenceId);
        this.actorReferenceId = uniqueId.toString();
    }

    @Override
    public boolean isActor(Player player) {
        return player.getUniqueId().equals(uniqueId);
    }

    @Override
    public String getActorType() {
        return "player";
    }
}
