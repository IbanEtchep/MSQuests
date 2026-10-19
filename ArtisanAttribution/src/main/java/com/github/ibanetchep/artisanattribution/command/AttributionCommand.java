package com.github.ibanetchep.artisanattribution.command;

import com.github.ibanetchep.artisanattribution.AttributionPlugin;
import com.github.ibanetchep.artisanattribution.AttributionService;
import com.github.ibanetchep.artisanattribution.Messages;
import com.github.ibanetchep.artisanattribution.config.AttributionSource;
import com.github.ibanetchep.artisanattribution.storage.PlayerAttribution;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * {@code /attribution}: a player opens the menu for themselves. Admins
 * ({@code artisanattribution.admin}): {@code open <player>}, {@code stats}, {@code reset <player>},
 * {@code reload}.
 */
public final class AttributionCommand implements BasicCommand {

    public static final String PERMISSION = "artisanattribution.command";
    public static final String ADMIN_PERMISSION = "artisanattribution.admin";

    private static final List<String> ADMIN_SUBCOMMANDS = List.of("open", "stats", "reset", "reload");

    private final AttributionPlugin plugin;
    private final AttributionService service;
    private final Messages messages;

    public AttributionCommand(AttributionPlugin plugin, AttributionService service, Messages messages) {
        this.plugin = plugin;
        this.service = service;
        this.messages = messages;
    }

    @Override
    public @Nullable String permission() {
        return PERMISSION;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length == 0) {
            openOwn(sender);
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!ADMIN_SUBCOMMANDS.contains(sub)) {
            messages.send(sender, "command.usage");
            return;
        }
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            messages.send(sender, "command.no_permission");
            return;
        }
        switch (sub) {
            case "open" -> openFor(sender, args);
            case "stats" -> stats(sender);
            case "reset" -> reset(sender, args);
            case "reload" -> plugin.reloadAttribution(sender);
            default -> messages.send(sender, "command.usage");
        }
    }

    private void openOwn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.players_only");
            return;
        }
        PlayerAttribution state = service.state(player.getUniqueId());
        if (state != null && state.answered()) {
            messages.send(player, "answer.already", Map.of("source", service.sourceName(player, state.source())));
            return;
        }
        if (!service.openMenu(player)) {
            messages.send(player, "menu.missing", Map.of("menu", service.config().menu()));
        }
    }

    private void openFor(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.send(sender, "command.usage");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "command.player_offline", Map.of("player", args[1]));
            return;
        }
        if (service.openMenu(target)) {
            messages.send(sender, "command.opened", Map.of("player", target.getName()));
        } else {
            messages.send(sender, "menu.missing", Map.of("menu", service.config().menu()));
        }
    }

    private void stats(CommandSender sender) {
        service.stats().whenComplete((counts, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Could not read the attribution statistics", error);
                messages.send(sender, "command.error");
                return;
            }
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) {
                messages.send(sender, "stats.empty");
                return;
            }
            messages.send(sender, "stats.header", Map.of("total", total));
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                AttributionSource source = service.config().source(entry.getKey());
                String percent = String.format(Locale.ROOT, "%.1f", entry.getValue() * 100.0 / total);
                messages.send(sender, source != null ? "stats.line" : "stats.removed", Map.of(
                        "source", service.sourceName(sender, entry.getKey()),
                        "count", entry.getValue(),
                        "percent", percent));
            }
        });
    }

    private void reset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.send(sender, "command.usage");
            return;
        }
        String name = args[1];
        UUID playerId = resolve(name);
        if (playerId == null) {
            messages.send(sender, "command.player_not_found", Map.of("player", name));
            return;
        }
        service.reset(playerId).whenComplete((cleared, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Could not reset the attribution of " + name, error);
                messages.send(sender, "command.error");
                return;
            }
            messages.send(sender, cleared ? "command.reset" : "command.reset_none", Map.of("player", name));
        });
    }

    /** An online player, else one this server has seen (no lookup on Mojang's servers). */
    private static @Nullable UUID resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        return cached != null ? cached.getUniqueId() : null;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        if (!stack.getSender().hasPermission(ADMIN_PERMISSION)) return List.of();
        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return ADMIN_SUBCOMMANDS.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("reset"))) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
