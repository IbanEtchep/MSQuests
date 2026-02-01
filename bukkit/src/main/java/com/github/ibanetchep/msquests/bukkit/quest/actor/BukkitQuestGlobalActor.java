package com.github.ibanetchep.msquests.bukkit.quest.actor;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

import java.util.UUID;

public class BukkitQuestGlobalActor extends QuestActor {

    public BukkitQuestGlobalActor(UUID id, String name) {
        super(id, name);
    }

    @Override
    public boolean isMember(UUID playerId) {
        return true;
    }

    @Override
    public String getActorType() {
        return "global";
    }
}