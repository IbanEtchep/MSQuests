package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Bossbar shown to each online player that is actor of the quest, using Adventure API.
 */

@ActionType("boss_bar")
@AtLeastOneOfFields({"message", "message_key"})
public class PlayerBossBarAction extends BukkitQuestAction {

    @ConfigField(name = "message")
    private final @Nullable String message;

    @ConfigField(name = "message_key")
    private final @Nullable String messageKey;

    @ConfigField(name = "color")
    private final BossBar.Color color;

    @ConfigField(name = "style")
    private final BossBar.Overlay style;

    @ConfigField(name = "show_progress")
    private final boolean showProgress;

    @ConfigField(name = "duration")
    private final int duration;

    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, WrappedTask> removeTasks = new ConcurrentHashMap<>();

    public PlayerBossBarAction(QuestActionDTO dto, BukkitQuestsPlugin plugin) {
        super(dto, plugin);
        this.message = (String) dto.params().get("message");
        this.messageKey = (String) dto.params().get("message_key");
        this.duration = Integer.parseInt(dto.params().getOrDefault("duration", "5").toString());
        this.showProgress = Boolean.parseBoolean(
                Objects.toString(dto.params().getOrDefault("show_progress", "true"))
        );

        String colorStr = (String) dto.params().getOrDefault("color", "WHITE");
        String styleStr = (String) dto.params().getOrDefault("style", "PROGRESS");
        this.color = BossBar.Color.valueOf(colorStr.toUpperCase());
        this.style = BossBar.Overlay.valueOf(styleStr.toUpperCase());
    }

    @Override
    protected void perform(Quest quest) {
        double progress = quest.getProgressRatio();

        Component text = MessageBuilder.raw(resolveMessage())
                .applyPlaceholderResolver(quest)
                .toComponent();

        showOrUpdateBossBar(quest, text, progress);
    }

    @Override
    protected void perform(QuestObjective objective) {
        double progress = objective.getProgressRatio();

        Component text = MessageBuilder.raw(resolveMessage())
                .applyPlaceholderResolver(objective)
                .toComponent();

        showOrUpdateBossBar(objective.getQuest(), text, progress);
    }

    private void showOrUpdateBossBar(Quest quest, Component text, double progress) {
        float barProgress = showProgress ? (float) progress : 1.0f;

        for (Player player : getOnlinePlayers(quest)) {
            UUID uuid = player.getUniqueId();
            BossBar existing = bossBars.get(uuid);

            if (existing != null) {
                existing.name(text);
                existing.progress(barProgress);
                existing.color(color);
                existing.overlay(style);
            } else {
                BossBar bar = BossBar.bossBar(text, barProgress, color, style);
                bossBars.put(uuid, bar);
                player.showBossBar(bar);
            }

            WrappedTask previous = removeTasks.remove(uuid);
            if (previous != null) previous.cancel();

            WrappedTask task = plugin.getScheduler().runLater(() -> {
                BossBar bar = bossBars.remove(uuid);
                removeTasks.remove(uuid);
                if (bar == null) return;
                Player viewer = Bukkit.getPlayer(uuid);
                if (viewer != null) viewer.hideBossBar(bar);
            }, duration, TimeUnit.SECONDS);
            removeTasks.put(uuid, task);
        }
    }

    private String resolveMessage() {
        if (messageKey != null) {
            return MessageBuilder.translatable(messageKey).toStringRaw();
        }
        return Objects.requireNonNullElse(message, "");
    }

    @Override
    public QuestActionDTO toDTO() {
        Map<String, Object> config = new HashMap<>();
        if (message != null) config.put("message", message);
        if (messageKey != null) config.put("message_key", messageKey);
        config.put("color", color.name());
        config.put("style", style.name());
        config.put("show_progress", showProgress);
        config.put("duration", duration);
        return new QuestActionDTO(getType(), getName(), config, null);
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "message", message != null ? message : "",
                "message_key", messageKey != null ? messageKey : "",
                "color", color.name(),
                "style", style.name()
        );
    }
}
