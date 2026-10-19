package com.github.ibanetchep.artisanattribution.listener;

import com.github.ibanetchep.artisanattribution.AttributionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Loads a player's answer before they join, asks them on join, forgets them on quit. */
public final class PlayerConnectionListener implements Listener {

    private final AttributionService service;

    public PlayerConnectionListener(AttributionService service) {
        this.service = service;
    }

    /** Async and allowed to block: the answer is in memory by the time the player joins. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        service.load(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        service.onJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.forget(event.getPlayer().getUniqueId());
    }
}
