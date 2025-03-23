package com.github.ibanetchep.msquests.core.quest.actor;

import java.util.UUID;

public class QuestPlayerActor extends QuestActor {

    public QuestPlayerActor(UUID id) {
        super(id);
    }

    @Override
    public boolean isActor(UUID playerId) {
        return playerId.equals(id);
    }

    @Override
    public String getActorType() {
        return "player";
    }
}
