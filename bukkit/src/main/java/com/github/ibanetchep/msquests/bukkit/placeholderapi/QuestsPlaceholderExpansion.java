package com.github.ibanetchep.msquests.bukkit.placeholderapi;

import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestsPlaceholderExpansion extends PlaceholderExpansion {

    private static final Pattern OBJECTIVE_PATTERN = Pattern.compile("tracked_quest_objective_(\\d+)");
    private static final Pattern GROUP_PATTERN = Pattern.compile("group_(.+)_(period_end|countdown)");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final int MAX_OBJECTIVES = 9;

    private final PlayerProfileRegistry playerProfileRegistry;
    private final QuestConfigRegistry questConfigRegistry;

    public QuestsPlaceholderExpansion(PlayerProfileRegistry playerProfileRegistry, QuestConfigRegistry questConfigRegistry) {
        this.playerProfileRegistry = playerProfileRegistry;
        this.questConfigRegistry = questConfigRegistry;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "msquests";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Iban";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Nullable
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        PlayerProfile profile = playerProfileRegistry.getPlayerProfile(player.getUniqueId());
        if (profile == null) {
            return "";
        }

        Quest quest = profile.getTrackedQuest();

        switch (params.toLowerCase()) {
            case "tracked_name":
                return getQuestName(quest);

            case "tracked":
                return getQuestPlaceholder(quest);

            case "tracked_stage":
                return getStagePlaceholder(quest);

            case "tracked_stage_count":
                return getStageCount(quest);

            case "tracked_stage_index":
                return getStageIndex(quest);

            case "tracked_objective_firstactive":
                return getFirstActiveObjectivePlaceholder(quest);

            case "tracked_objective_firstactive_index":
                return getFirstActiveObjectiveIndex(quest);

            case "tracked_stage_objective_count":
                return getObjectiveCount(quest);
        }

        Matcher matcher = OBJECTIVE_PATTERN.matcher(params.toLowerCase());
        if (matcher.matches()) {
            int objectiveIndex = Integer.parseInt(matcher.group(1));
            if (objectiveIndex >= 1 && objectiveIndex <= MAX_OBJECTIVES) {
                return getObjectivePlaceholder(quest, objectiveIndex);
            }
        }

        Matcher groupMatcher = GROUP_PATTERN.matcher(params.toLowerCase());
        if (groupMatcher.matches()) {
            String groupKey = groupMatcher.group(1);
            String type = groupMatcher.group(2);
            QuestGroupConfig group = questConfigRegistry.getQuestGroupConfigs().get(groupKey);
            if (group == null) return "";

            Instant nextReset = group.getNextReset();
            Instant periodEnd = nextReset != null ? nextReset : group.getPeriodEnd();
            if (periodEnd == null) return "";

            return switch (type) {
                case "period_end" -> formatTime(periodEnd);
                case "countdown" -> formatCountdown(periodEnd);
                default -> "";
            };
        }

        return "";
    }

    @Nullable
    private String getQuestName(@Nullable Quest quest) {
        return quest != null ? quest.getQuestConfig().getName() : "";
    }

    @Nullable
    private String getQuestPlaceholder(@Nullable Quest quest) {
        if (quest == null) {
            return "";
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_QUEST)
                .applyPlaceholderResolver(quest)
                .toStringRaw();
    }

    @Nullable
    private String getStagePlaceholder(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return "";
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_STAGE)
                .applyPlaceholderResolver(stage)
                .toStringRaw();
    }

    @Nullable
    private String getStageCount(@Nullable Quest quest) {
        return quest != null ? String.valueOf(quest.getStages().size()) : null;
    }

    private String getStageIndex(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return "";
        }

        return String.valueOf(quest.getStagesList().indexOf(stage));
    }

    @Nullable
    private String getObjectivePlaceholder(@Nullable Quest quest, int index) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return "";
        }

        List<QuestObjective> objectives = stage.getObjectives().values().stream().toList();
        if (objectives.size() < index) {
            return "";
        }

        QuestObjective objective = objectives.get(index - 1);
        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_OBJECTIVE)
                .applyPlaceholderResolver(objective)
                .toStringRaw();
    }

    @Nullable
    private String getFirstActiveObjectivePlaceholder(@Nullable Quest quest) {
        QuestObjective objective = getFirstActiveObjective(quest);
        if (objective == null) {
            return "";
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_OBJECTIVE)
                .applyPlaceholderResolver(objective)
                .toStringRaw();
    }

    private String getFirstActiveObjectiveIndex(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        QuestObjective objective = getFirstActiveObjective(quest);

        if (stage == null || objective == null) {
            return "";
        }

        return String.valueOf(stage.getObjectiveList().indexOf(objective));
    }

    @Nullable
    private String getObjectiveCount(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        return stage != null ? String.valueOf(stage.getObjectives().size()) : null;
    }

    @Nullable
    private QuestStage getCurrentStage(@Nullable Quest quest) {
        return quest != null ? quest.getCurrentStage() : null;
    }

    @Nullable
    private QuestObjective getFirstActiveObjective(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        return stage != null ? stage.getFirstActiveObjective() : null;
    }

    private String formatTime(Instant instant) {
        return TIME_FORMATTER.format(instant);
    }

    private String formatCountdown(Instant periodEnd) {
        Duration remaining = Duration.between(Instant.now(), periodEnd);
        if (remaining.isNegative() || remaining.isZero()) return "0s";

        long hours = remaining.toHours();
        long minutes = remaining.toMinutesPart();
        long seconds = remaining.toSecondsPart();

        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}