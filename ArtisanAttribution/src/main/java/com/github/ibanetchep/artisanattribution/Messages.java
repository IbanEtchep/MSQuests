package com.github.ibanetchep.artisanattribution;

import net.artisanmc.modules.api.translation.MessageBundle;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * The plugin's system messages, served by Artisan's translations (namespace {@code attribution}):
 * {@code lang/<code>.yml} in the jar, overridden key by key by
 * {@code plugins/ArtisanAttribution/lang/<code>.yml}.
 */
public final class Messages {

    public static final String NAMESPACE = "attribution";

    private final MessageBundle bundle;

    public Messages(MessageBundle bundle) {
        this.bundle = bundle;
    }

    public void send(CommandSender to, String key) {
        to.sendMessage(bundle.get(to, key));
    }

    /** Placeholder values are inserted as plain text: a player name cannot inject MiniMessage tags. */
    public void send(CommandSender to, String key, Map<String, ?> placeholders) {
        to.sendMessage(bundle.get(to, key, placeholders));
    }

    public void reload() {
        bundle.reload();
    }
}
