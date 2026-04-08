package com.github.ibanetchep.msquests.bukkit.listener;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.TimeUnit;

public class PlayerJoinListener implements Listener {

    private final BukkitQuestsPlugin plugin;

    public PlayerJoinListener(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getQuestPlayerService().loadPlayer(player).thenRun(() -> {
            plugin.getScheduler().runLater(() -> {
                if (plugin.getTrackingBossBarService().isEnabled()) {
                    plugin.getTrackingBossBarService().showBossBar(player);
                }
            }, 1, TimeUnit.SECONDS);
        });
    }

}
