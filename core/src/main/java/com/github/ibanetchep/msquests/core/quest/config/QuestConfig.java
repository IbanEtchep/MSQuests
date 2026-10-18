package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import org.jetbrains.annotations.Nullable;

import java.util.*;


public class QuestConfig implements PlaceholderProvider {

    private String key;
    private @Nullable String uid;
    private String name;
    private String description;
    private final Map<String, QuestStageConfig> stages;
    private ActionChain rewards = ActionChain.EMPTY;
    private ActionChain startActions = ActionChain.EMPTY;
    private ActionChain objectiveProgressActions = ActionChain.EMPTY;
    private ActionChain objectiveCompleteActions = ActionChain.EMPTY;
    private long duration; // max in seconds
    private String tier;
    private QuestGroupConfig group;
    private Conditions requirements = Conditions.NONE;

    public QuestConfig(String key, String name, String description, long duration) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.stages = new LinkedHashMap<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Immutable id of a quest read from a {@code quest_group} Document, null for a quest read
     * from {@code quests/*.yml}. The database stores it instead of the editable key.
     */
    public @Nullable String getUid() {
        return uid;
    }

    public void setUid(@Nullable String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    /** Chain run for the actor's members when the quest completes. */
    public ActionChain getRewards() {
        return rewards;
    }

    public void setRewards(ActionChain rewards) {
        this.rewards = rewards;
    }

    /** Chain run when the quest starts, after the group's {@code quest_start}. */
    public ActionChain getStartActions() {
        return startActions;
    }

    public void setStartActions(ActionChain startActions) {
        this.startActions = startActions;
    }

    /** Chain run when one of the quest's objectives progresses, after the group's {@code objective_progress}. */
    public ActionChain getObjectiveProgressActions() {
        return objectiveProgressActions;
    }

    public void setObjectiveProgressActions(ActionChain objectiveProgressActions) {
        this.objectiveProgressActions = objectiveProgressActions;
    }

    /** Chain run when one of the quest's objectives completes, after the group's {@code objective_complete}. */
    public ActionChain getObjectiveCompleteActions() {
        return objectiveCompleteActions;
    }

    public void setObjectiveCompleteActions(ActionChain objectiveCompleteActions) {
        this.objectiveCompleteActions = objectiveCompleteActions;
    }

    public void setGroup(QuestGroupConfig group) {
        this.group = group;
    }

    public QuestGroupConfig getGroupConfig() {
        return group;
    }

    public Map<String, QuestStageConfig> getStages() {
        return stages;
    }

    public void addStage(QuestStageConfig stage) {
        stages.put(stage.getKey(), stage);
    }

    /** Conditions the starting player must meet; checked by a {@code ConditionChecker}. */
    public Conditions getRequirements() {
        return requirements;
    }

    public void setRequirements(Conditions requirements) {
        this.requirements = requirements;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quest_key", key);
        placeholders.put("quest_name", name);
        placeholders.put("quest_description", description);

        if (tier != null) {
            placeholders.put("quest_tier", tier);
            if (group != null) {
                QuestTierConfig tierConfig = group.getTier(tier);
                if (tierConfig != null) {
                    placeholders.put("quest_tier_name", tierConfig.getName());
                }
            }
        }

        return placeholders;
    }
}
