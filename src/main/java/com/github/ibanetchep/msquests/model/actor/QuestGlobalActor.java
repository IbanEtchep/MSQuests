package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestGlobalActor extends QuestActor {

    public QuestGlobalActor(UUID uniqueId, String actorReferenceId) {
        super(uniqueId, actorReferenceId);
        this.actorReferenceId = "global";
    }

    @Override
    public boolean isActor(Player player) {
        return player.getUniqueId().equals(uniqueId);
    }

    @Override
    public String getActorType() {
        return "global";
    }
}
