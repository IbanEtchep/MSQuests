package com.github.ibanetchep.msquests.bukkit.quest.actor;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

import java.util.UUID;

public class BukkitQuestPlayerActor extends QuestActor {

    public BukkitQuestPlayerActor(UUID id, String name) {
        super(id, name);
    }

    @Override
    public boolean isMember(UUID playerId) {
        return playerId.equals(id);
    }

    @Override
    public String getActorType() {
        return "player";
    }
}
