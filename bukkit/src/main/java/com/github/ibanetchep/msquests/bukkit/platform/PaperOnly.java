package com.github.ibanetchep.msquests.bukkit.platform;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/** Every call into Adventure. Loaded only when {@link Platform#PAPER}. */
final class PaperOnly {

    private PaperOnly() {
    }

    static void actionBar(Player player, String miniMessage) {
        player.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage));
    }
}
