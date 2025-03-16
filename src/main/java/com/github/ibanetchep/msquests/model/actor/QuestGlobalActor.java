package com.github.ibanetchep.msquests.model.actor;

import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestGlobalActor extends QuestActor {

    public QuestGlobalActor(UUID id, String actorReferenceId) {
        super(id, actorReferenceId);
        this.actorReferenceId = "global";
    }

    @Override
    public boolean isActor(Player player) {
        return true;
    }

    @Override
    public String getActorType() {
        return "global";
    }
}
