package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

/**
 * Quests are loaded on server load to let expansions load their stuff.
 */
public class ServerLoadListener implements Listener {

    private final BukkitQuestsPlugin plugin;

    public ServerLoadListener(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        // Actors registered by other plugins during their onEnable (MSGuildQuests' guilds) were
        // loaded before any group existed: their saved quests were dropped and nothing was
        // distributed. Reload them now that the groups are known.
        plugin.loadQuestContent()
                .thenCompose(v -> plugin.getQuestActorService().reloadActors())
                .thenCompose(v -> plugin.getQuestPlayerService().loadAllPlayers())
                .thenRun(() -> plugin.getLogger().info("Quests loaded"));
    }
}
