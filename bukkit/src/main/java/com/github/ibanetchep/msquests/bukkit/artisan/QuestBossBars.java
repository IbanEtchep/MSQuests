package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.platform.BarOverlay;
import com.github.ibanetchep.msquests.bukkit.platform.BossBars;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
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
    private final BossBars factory;
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final Map<UUID, WrappedTask> hideTasks = new ConcurrentHashMap<>();

    QuestBossBars(BukkitQuestsPlugin plugin, BossBars factory) {
        this.plugin = plugin;
        this.factory = factory;
    }

    void show(Player player, String legacyTitle, float progress, BarColor color, BarOverlay overlay, int seconds) {
        UUID id = player.getUniqueId();
        BossBar bar = bars.get(id);
        if (bar != null) {
            bar.setTitle(legacyTitle);
            bar.setColor(color);
            bar.setStyle(overlay.style());
        } else {
            bar = factory.create(legacyTitle, color, overlay.style());
            bars.put(id, bar);
            bar.addPlayer(player);
        }
        bar.setProgress(progress);

        WrappedTask previous = hideTasks.remove(id);
        if (previous != null) previous.cancel();
        hideTasks.put(id, plugin.getScheduler().runLater(() -> hide(id), Math.max(1, seconds), TimeUnit.SECONDS));
    }

    private void hide(UUID id) {
        hideTasks.remove(id);
        BossBar bar = bars.remove(id);
        if (bar != null) bar.removeAll();
    }
}
