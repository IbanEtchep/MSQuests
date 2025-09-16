package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
import org.bukkit.entity.Player;

public class QuestPlayerActorService {

    private final MSQuestsPlugin plugin;

    public QuestPlayerActorService(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerActor(Player player) {
        QuestPlayerActor actor = new QuestPlayerActor(player.getUniqueId(), player.getName());
        plugin.getQuestPersistenceService().loadActor(actor);
    }
}
