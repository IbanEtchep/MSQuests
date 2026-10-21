package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.config.TrackingBossBarConfig;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingBossBarService {

    private final TrackingBossBarConfig config;
    private final PlayerProfileRegistry profileRegistry;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();

    public TrackingBossBarService(TrackingBossBarConfig config, PlayerProfileRegistry profileRegistry) {
        this.config = config;
        this.profileRegistry = profileRegistry;
    }

    public boolean isEnabled() {
        return config.enabled();
    }

    public void refreshBossBar(Player player) {
        if (!config.enabled()) return;

        PlayerProfile profile = profileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) {
            hideBossBar(player);
            return;
        }

        Quest quest = profile.getTrackedQuest();
        if (quest == null || !quest.isActive()) {
            hideBossBar(player);
            return;
        }

        QuestObjective objective = quest.getFirstActiveObjective();
        if (objective == null) {
            hideBossBar(player);
            return;
        }

        String text = MessageBuilder.legacy(resolveMessage(quest, objective));
        float progress = computeProgress(objective);

        BossBar existing = bossBars.get(player.getUniqueId());
        if (existing != null) {
            existing.setTitle(text);
            existing.setProgress(progress);
        } else {
            BossBar bossBar = Bukkit.createBossBar(text, config.color(), config.style().style());
            bossBar.setProgress(progress);
            bossBars.put(player.getUniqueId(), bossBar);
            bossBar.addPlayer(player);
        }
    }

    public void showBossBar(Player player) {
        refreshBossBar(player);
    }

    public void hideBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void onQuestProgress(QuestObjective objective) {
        Quest quest = objective.getQuest();
        for (PlayerProfile profile : quest.getActor().getProfiles()) {
            if (!quest.getId().equals(profile.getTrackedQuestId())) continue;
            Player player = Bukkit.getPlayer(profile.getId());
            if (player != null) {
                refreshBossBar(player);
            }
        }
    }

    public void onQuestComplete(Quest quest) {
        for (PlayerProfile profile : quest.getActor().getProfiles()) {
            if (!quest.getId().equals(profile.getTrackedQuestId())) continue;
            Player player = Bukkit.getPlayer(profile.getId());
            if (player != null) {
                hideBossBar(player);
            }
        }
    }

    public void refreshAll() {
        if (!config.enabled()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshBossBar(player);
        }
    }

    private float computeProgress(QuestObjective objective) {
        if (objective == null) return 1.0f;
        if (!config.showProgress()) return 1.0f;
        float progress = (float) objective.getProgressRatio();
        return Math.min(1.0f, Math.max(0.0f, progress));
    }

    private String resolveMessage(Quest quest, QuestObjective objective) {
        String message = config.message();
        if (objective != null) {
            return MessageBuilder.raw(message)
                    .applyPlaceholderResolver(objective)
                    .toStringRaw();
        }
        return MessageBuilder.raw(message)
                .applyPlaceholderResolver(quest)
                .toStringRaw();
    }
}
