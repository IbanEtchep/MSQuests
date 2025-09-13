package com.github.ibanetchep.msquests.bukkit.quest.actor;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestPlayerActor extends QuestActor {

    public QuestPlayerActor(UUID id, String name) {
        super(id, name);
    }

    @Override
    public boolean isActor(UUID playerId) {
        return playerId.equals(id);
    }

    @Override
    public void sendMessage(String message) {
        Player player = Bukkit.getPlayer(id);

        if (player == null) {
            return;
        }

        player.sendMessage(message);
    }

    @Override
    public String getActorType() {
        return "player";
    }
}
