package com.github.ibanetchep.msquests.bukkit.quest.actor;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

import java.util.UUID;

public class QuestGlobalActor extends QuestActor {

    public QuestGlobalActor(UUID id, String name) {
        super(id, name);
    }

    @Override
    public boolean isActor(UUID playerId) {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        //TODO broadcast
    }

    @Override
    public String getActorType() {
        return "global";
    }
}
