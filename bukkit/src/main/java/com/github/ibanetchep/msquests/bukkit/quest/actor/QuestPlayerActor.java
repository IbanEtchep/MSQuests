package com.github.ibanetchep.msquests.bukkit.quest.actor;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestPlayerActor extends QuestActor {

    public QuestPlayerActor(UUID id, String name) {
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
