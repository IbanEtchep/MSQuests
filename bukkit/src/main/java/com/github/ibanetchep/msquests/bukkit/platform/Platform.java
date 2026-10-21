package com.github.ibanetchep.msquests.bukkit.platform;

import org.bukkit.entity.Player;

/**
 * Paper (and its forks) or Spigot, decided once by the presence of a Paper-only class. Paper-only
 * calls live in {@link PaperOnly}, which is loaded only when {@link #PAPER} is true: a class that
 * names Adventure types fails to load on Spigot even if those methods never run.
 */
public final class Platform {

    public static final boolean PAPER = classExists("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");

    private Platform() {
    }

    /** Shows {@code miniMessage} above the hotbar. */
    @SuppressWarnings("deprecation") // Spigot's only action bar API
    public static void actionBar(Player player, String miniMessage, String legacy) {
        if (PAPER) {
            PaperOnly.actionBar(player, miniMessage);
        } else {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(legacy));
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
