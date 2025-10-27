package com.github.ibanetchep.msquests.bukkit.placeholderapi;

import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestsPlaceholderExpansion extends PlaceholderExpansion {

    private static final Pattern OBJECTIVE_PATTERN = Pattern.compile("tracked_quest_objective_(\\d+)");
    private static final int MAX_OBJECTIVES = 9;

    private final PlayerProfileRegistry playerProfileRegistry;

    public QuestsPlaceholderExpansion(PlayerProfileRegistry playerProfileRegistry) {
        this.playerProfileRegistry = playerProfileRegistry;
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
            return null;
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

        return null;
    }

    @Nullable
    private String getQuestName(@Nullable Quest quest) {
        return quest != null ? quest.getQuestConfig().getName() : null;
    }

    @Nullable
    private String getQuestPlaceholder(@Nullable Quest quest) {
        if (quest == null) {
            return null;
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_QUEST)
                .applyPlaceholderResolver(quest)
                .toStringRaw();
    }

    @Nullable
    private String getStagePlaceholder(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return null;
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_STAGE)
                .applyPlaceholderResolver(stage)
                .toStringRaw();
    }

    @Nullable
    private String getStageCount(@Nullable Quest quest) {
        return quest != null ? String.valueOf(quest.getStages().size()) : null;
    }

    @Nullable
    private String getStageIndex(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return null;
        }

        return String.valueOf(quest.getStagesList().indexOf(stage));
    }

    @Nullable
    private String getObjectivePlaceholder(@Nullable Quest quest, int index) {
        QuestStage stage = getCurrentStage(quest);
        if (stage == null) {
            return null;
        }

        List<QuestObjective> objectives = stage.getObjectives().values().stream().toList();
        if (objectives.size() < index) {
            return null;
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
            return null;
        }

        return MessageBuilder.translatable(TranslationKey.PLACEHOLDER_OBJECTIVE)
                .applyPlaceholderResolver(objective)
                .toStringRaw();
    }

    @Nullable
    private String getFirstActiveObjectiveIndex(@Nullable Quest quest) {
        QuestStage stage = getCurrentStage(quest);
        QuestObjective objective = getFirstActiveObjective(quest);

        if (stage == null || objective == null) {
            return null;
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
}