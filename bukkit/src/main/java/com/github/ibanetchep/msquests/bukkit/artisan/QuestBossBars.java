package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The boss bar of {@code quests:boss_bar}: one per player, updated in place by each new call and
 * hidden {@code duration} seconds after the last one (the bar MSQuests' {@code boss_bar} action
 * showed for a progressing objective).
 */
final class QuestBossBars {

    private final BukkitQuestsPlugin plugin;
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final Map<UUID, WrappedTask> hideTasks = new ConcurrentHashMap<>();

    QuestBossBars(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    void show(Player player, Component text, float progress, BossBar.Color color, BossBar.Overlay overlay, int seconds) {
        UUID id = player.getUniqueId();
        BossBar bar = bars.get(id);
        if (bar != null) {
            bar.name(text);
            bar.progress(progress);
            bar.color(color);
            bar.overlay(overlay);
        } else {
            bar = BossBar.bossBar(text, progress, color, overlay);
            bars.put(id, bar);
            player.showBossBar(bar);
        }

        WrappedTask previous = hideTasks.remove(id);
        if (previous != null) previous.cancel();
        hideTasks.put(id, plugin.getScheduler().runLater(() -> hide(id), Math.max(1, seconds), TimeUnit.SECONDS));
    }

    private void hide(UUID id) {
        hideTasks.remove(id);
        BossBar bar = bars.remove(id);
        Player viewer = Bukkit.getPlayer(id);
        if (bar != null && viewer != null) viewer.hideBossBar(bar);
    }
}
