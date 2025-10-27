package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    private BossBar bossBar;
    private WrappedTask removeTask;

    public PlayerBossBarAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
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
    public void execute(Quest quest) {
        double progress = quest.getProgressPercent();

        Component text = MessageBuilder.raw(resolveMessage())
                .applyPlaceholderResolver(quest)
                .toComponent();

        showOrUpdateBossBar(quest, text, progress);
    }

    @Override
    public void execute(QuestObjective objective) {
        double progress = objective.getProgressRatio();

        Component text = MessageBuilder.raw(resolveMessage())
                .applyPlaceholderResolver(objective)
                .toComponent();

        showOrUpdateBossBar(objective.getQuest(), text, progress);
    }

    private void showOrUpdateBossBar(Quest quest, Component text, double progress) {
        if (bossBar != null) {
            bossBar.name(text);
            bossBar.progress(showProgress ? (float) progress : 1.0f);
            bossBar.color(color);
            bossBar.overlay(style);
            if (removeTask != null) removeTask.cancel();
        } else {
            bossBar = BossBar.bossBar(text, showProgress ? (float) progress : 1.0f, color, style);
            getOnlinePlayers(quest).forEach(player -> player.showBossBar(bossBar));
        }

        removeTask = plugin.getScheduler().runLater(() -> {
            getOnlinePlayers(quest).forEach(player -> player.hideBossBar(bossBar));
            bossBar = null;
        }, duration, TimeUnit.SECONDS);
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
        return new QuestActionDTO(getType(), getName(), config);
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "message", message != null ? message : "",
                "message_key", messageKey != null ? messageKey : "",
                "color", color.name(),
                "style", style.name()
        );
    }
}
