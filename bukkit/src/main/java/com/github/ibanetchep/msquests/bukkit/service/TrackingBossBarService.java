package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.config.TrackingBossBarConfig;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

    public void showBossBar(Player player) {
        if (!config.enabled()) return;

        PlayerProfile profile = profileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) return;

        Quest quest = profile.getTrackedQuest();
        if (quest == null) return;

        QuestObjective objective = quest.getFirstActiveObjective();

        Component text = resolveMessage(quest, objective);
        float progress = computeProgress(objective);

        BossBar existing = bossBars.get(player.getUniqueId());
        if (existing != null) {
            existing.name(text);
            existing.progress(progress);
        } else {
            BossBar bossBar = BossBar.bossBar(text, progress, config.color(), config.style());
            bossBars.put(player.getUniqueId(), bossBar);
            player.showBossBar(bossBar);
        }
    }

    public void updateBossBar(Player player) {
        BossBar existing = bossBars.get(player.getUniqueId());
        if (existing == null) return;

        PlayerProfile profile = profileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) return;

        Quest quest = profile.getTrackedQuest();
        if (quest == null) {
            hideBossBar(player);
            return;
        }

        QuestObjective objective = quest.getFirstActiveObjective();
        Component text = resolveMessage(quest, objective);
        float progress = computeProgress(objective);

        existing.name(text);
        existing.progress(progress);
    }

    public void hideBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void onQuestProgress(QuestObjective objective) {
        Quest quest = objective.getQuest();
        for (PlayerProfile profile : quest.getActor().getProfiles()) {
            if (!quest.getId().equals(profile.getTrackedQuestId())) continue;
            Player player = Bukkit.getPlayer(profile.getId());
            if (player != null) {
                updateBossBar(player);
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

    private float computeProgress(QuestObjective objective) {
        if (objective == null) return 1.0f;
        if (!config.showProgress()) return 1.0f;
        float progress = (float) objective.getProgressRatio() / 100f;
        return Math.min(1.0f, Math.max(0.0f, progress));
    }

    private Component resolveMessage(Quest quest, QuestObjective objective) {
        String message = config.message();
        if (objective != null) {
            return MessageBuilder.raw(message)
                    .applyPlaceholderResolver(objective)
                    .toComponent();
        }
        return MessageBuilder.raw(message)
                .applyPlaceholderResolver(quest)
                .toComponent();
    }
}
