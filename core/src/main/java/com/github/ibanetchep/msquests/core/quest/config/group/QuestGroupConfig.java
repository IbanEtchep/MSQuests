package com.github.ibanetchep.msquests.core.quest.config.group;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestGroupConfig implements PlaceholderProvider {

    /** {@link #getObjectiveLine()} of a group that sets none: MSQuests' usual list item. */
    public static final String DEFAULT_OBJECTIVE_LINE = "<newline> - {objective_name}";

    private final String key;
    private final @Nullable String uid;
    private final @Nullable String sourceProject;
    private final String name;
    private final String description;
    private final String actorType;

    private final Map<String, QuestConfig> questConfigs;
    private final List<QuestConfig> orderedQuests;

    private final @Nullable Instant startAt;
    private final @Nullable Instant endAt;

    private final @Nullable String resetCron;
    private final @Nullable ZoneId resetZone;
    private final @Nullable Integer maxPerPeriod;
    private final int maxActive;

    private final ActionChain questStartActions;
    private final ActionChain questCompleteActions;
    private final ActionChain objectiveProgressActions;
    private final ActionChain objectiveCompleteActions;
    private final ActionChain questDistributionActions;
    private final ActionChain actorLoadActions;
    private final ActionChain allPeriodQuestsCompleteActions;

    private final String objectiveLine;

    private final Map<String, QuestTierConfig> tiers;
    private final @Nullable Map<String, Integer> tierDistribution;

    private final boolean rotatable;
    private final @Nullable Integer maxRotationsPerPeriod;

    private final @Nullable DistributionConfig distributionConfig;

    private QuestGroupConfig(Builder builder) {
        this.key = builder.key;
        this.uid = builder.uid;
        this.sourceProject = builder.sourceProject;
        this.name = builder.name;
        this.description = builder.description;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.resetCron = builder.resetCron;
        this.resetZone = builder.resetZone;
        this.maxPerPeriod = builder.maxPerPeriod;
        this.maxActive = builder.maxActive;
        this.actorType = builder.actorType;
        this.questConfigs = new ConcurrentHashMap<>();
        this.orderedQuests = new CopyOnWriteArrayList<>();
        this.questStartActions = builder.questStartActions;
        this.questCompleteActions = builder.questCompleteActions;
        this.objectiveProgressActions = builder.objectiveProgressActions;
        this.objectiveCompleteActions = builder.objectiveCompleteActions;
        this.questDistributionActions = builder.questDistributionActions;
        this.actorLoadActions = builder.actorLoadActions;
        this.allPeriodQuestsCompleteActions = builder.allPeriodQuestsCompleteActions;
        this.objectiveLine = builder.objectiveLine != null ? builder.objectiveLine : DEFAULT_OBJECTIVE_LINE;
        this.tiers = builder.tiers != null ? builder.tiers : Map.of();
        this.tierDistribution = builder.tierDistribution;
        this.rotatable = builder.rotatable;
        this.maxRotationsPerPeriod = builder.maxRotationsPerPeriod;
        this.distributionConfig = builder.distributionConfig;
    }

    public String getKey() {
        return key;
    }

    /**
     * Immutable id of a group read from a {@code quest_group} Document, null for a group read
     * from {@code quests/*.yml}. The key is the Document's name and may be renamed; the uid is
     * what the database stores.
     */
    public @Nullable String getUid() {
        return uid;
    }

    /**
     * The Artisan project whose {@code quest_groups/} holds this group's Document: its action
     * chains run in that project, so their menu references resolve there. Null for a group read
     * from {@code quests/*.yml}.
     */
    public @Nullable String getSourceProject() {
        return sourceProject;
    }

    /** What rows referencing this group store: its uid when it has one, else its key. */
    public String getStorageId() {
        return uid != null ? uid : key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getActorType() {
        return actorType;
    }

    public Map<String, QuestConfig> getQuestConfigs() {
        return Collections.unmodifiableMap(questConfigs);
    }

    public List<QuestConfig> getOrderedQuests() {
        return Collections.unmodifiableList(orderedQuests);
    }

    public void addQuest(QuestConfig quest) {
        questConfigs.put(quest.getKey(), quest);
        orderedQuests.add(quest);
        quest.setGroup(this);
    }

    public void removeQuest(QuestConfig quest) {
        questConfigs.remove(quest.getKey());
        orderedQuests.remove(quest);
    }

    public boolean isActive() {
        return (startAt == null || startAt.isBefore(Instant.now())) &&
                (endAt == null || endAt.isAfter(Instant.now()));
    }

    public @Nullable Instant getStartAt() {
        return startAt;
    }

    public @Nullable Instant getEndAt() {
        return endAt;
    }

    public @Nullable String getResetCron() {
        return resetCron;
    }

    /** Zone the reset cron is read in; the server's zone when the group names none. */
    public ZoneId getResetZone() {
        return resetZone != null ? resetZone : ZoneId.systemDefault();
    }

    public @Nullable Integer getMaxPerPeriod() {
        return maxPerPeriod;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public ActionChain getQuestStartActions() {
        return questStartActions;
    }

    public ActionChain getQuestCompleteActions() {
        return questCompleteActions;
    }

    public ActionChain getObjectiveProgressActions() {
        return objectiveProgressActions;
    }

    public ActionChain getObjectiveCompleteActions() {
        return objectiveCompleteActions;
    }

    public ActionChain getQuestDistributionActions() {
        return questDistributionActions;
    }

    public ActionChain getActorLoadActions() {
        return actorLoadActions;
    }

    public ActionChain getAllPeriodQuestsCompleteActions() {
        return allPeriodQuestsCompleteActions;
    }

    /**
     * How the {@code {objectives}} binding of a chain renders one objective: {@code {name}}
     * placeholders of the objective ({@code {objective_name}}, {@code {objective_progress}}…),
     * the lines joined as they are.
     */
    public String getObjectiveLine() {
        return objectiveLine;
    }

    public Map<String, QuestTierConfig> getTiers() {
        return Collections.unmodifiableMap(tiers);
    }

    public @Nullable QuestTierConfig getTier(String key) {
        return tiers.get(key);
    }

    public @Nullable Map<String, Integer> getTierDistribution() {
        return tierDistribution;
    }

    public boolean hasTierDistribution() {
        return tierDistribution != null && !tierDistribution.isEmpty();
    }

    public boolean isRotatable() {
        return rotatable;
    }

    public @Nullable Integer getMaxRotationsPerPeriod() {
        return maxRotationsPerPeriod;
    }

    public @Nullable DistributionConfig getDistributionConfig() {
        return distributionConfig;
    }

    /**
     * The group's distribution strategy, {@link QuestDistributionStrategy#NONE} when it
     * declares none — a total answer, so a caller that needs only the strategy (and not the
     * amount or the triggers) does not re-handle the null itself.
     */
    public QuestDistributionStrategy getDistributionStrategy() {
        return distributionConfig != null ? distributionConfig.getStrategy() : QuestDistributionStrategy.NONE;
    }

    public boolean hasDistribution() {
        return distributionConfig != null;
    }

    public boolean hasDistributionTrigger(DistributionTrigger trigger) {
        return distributionConfig != null && distributionConfig.hasTrigger(trigger);
    }

    public @Nullable Instant getNextReset() {
        if(resetCron == null) {
            return null;
        }

        return CronUtils.getNextExecution(resetCron, Instant.now(), getResetZone());
    }

    public @Nullable Instant getPreviousReset() {
        if (resetCron == null) {
            return  null;
        }

        return CronUtils.getPreviousExecution(resetCron, Instant.now(), getResetZone());
    }

    public @Nullable Instant getPeriodEnd() {
        if(endAt != null) {
            return endAt;
        }

        return getNextReset();
    }

    public @Nullable Instant getPeriodStart() {
        if (getPreviousReset() != null) {
            return  getPreviousReset();
        }

        return startAt;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("group_key", key);
        placeholders.put("group_name", name != null ? name : "");
        placeholders.put("group_description", description != null ? description : "");
        return placeholders;
    }

    public static class Builder {
        private final String key;
        private final String name;
        private final String description;
        private final String actorType;

        private @Nullable String uid;
        private @Nullable String sourceProject;
        private @Nullable ZoneId resetZone;
        private @Nullable Instant startAt;
        private @Nullable Instant endAt;
        private @Nullable String resetCron;
        private @Nullable Integer maxPerPeriod = 1;
        private int maxActive = 1;

        private ActionChain questStartActions;
        private ActionChain questCompleteActions;
        private ActionChain objectiveProgressActions;
        private ActionChain objectiveCompleteActions;
        private ActionChain questDistributionActions;
        private ActionChain actorLoadActions;
        private ActionChain allPeriodQuestsCompleteActions;
        private @Nullable String objectiveLine;

        private @Nullable Map<String, QuestTierConfig> tiers;
        private @Nullable Map<String, Integer> tierDistribution;

        private boolean rotatable = false;
        private @Nullable Integer maxRotationsPerPeriod;

        private @Nullable DistributionConfig distributionConfig;

        public Builder(String key, String name, String description, String actorType) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.actorType = actorType;
            this.questStartActions = ActionChain.EMPTY;
            this.questCompleteActions = ActionChain.EMPTY;
            this.objectiveProgressActions = ActionChain.EMPTY;
            this.objectiveCompleteActions = ActionChain.EMPTY;
            this.questDistributionActions = ActionChain.EMPTY;
            this.actorLoadActions = ActionChain.EMPTY;
            this.allPeriodQuestsCompleteActions = ActionChain.EMPTY;
        }

        public Builder uid(@Nullable String uid) {
            this.uid = uid;
            return this;
        }

        public Builder sourceProject(@Nullable String sourceProject) {
            this.sourceProject = sourceProject;
            return this;
        }

        public Builder resetZone(@Nullable ZoneId resetZone) {
            this.resetZone = resetZone;
            return this;
        }

        public Builder startAt(Instant startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder endAt(Instant endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder resetCron(String resetCron) {
            this.resetCron = resetCron;
            return this;
        }

        public Builder maxPerPeriod(Integer maxPerPeriod) {
            this.maxPerPeriod = maxPerPeriod;
            return this;
        }

        public Builder maxActive(Integer maxActive) {
            this.maxActive = maxActive;
            return this;
        }

        public Builder questStartActions(ActionChain questStartActions) {
            this.questStartActions = questStartActions;
            return this;
        }

        public Builder questCompleteActions(ActionChain questCompleteActions) {
            this.questCompleteActions = questCompleteActions;
            return this;
        }

        public Builder objectiveProgressActions(ActionChain objectiveProgressActions) {
            this.objectiveProgressActions = objectiveProgressActions;
            return this;
        }

        public Builder objectiveCompleteActions(ActionChain objectiveCompleteActions) {
            this.objectiveCompleteActions = objectiveCompleteActions;
            return this;
        }

        public Builder questDistributionActions(ActionChain questDistributionActions) {
            this.questDistributionActions = questDistributionActions;
            return this;
        }

        public Builder actorLoadActions(ActionChain actorLoadActions) {
            this.actorLoadActions = actorLoadActions;
            return this;
        }

        public Builder allPeriodQuestsCompleteActions(ActionChain allPeriodQuestsCompleteActions) {
            this.allPeriodQuestsCompleteActions = allPeriodQuestsCompleteActions;
            return this;
        }

        public Builder objectiveLine(@Nullable String objectiveLine) {
            this.objectiveLine = objectiveLine;
            return this;
        }

        public Builder tiers(Map<String, QuestTierConfig> tiers) {
            this.tiers = tiers;
            return this;
        }

        public Builder tierDistribution(Map<String, Integer> tierDistribution) {
            this.tierDistribution = tierDistribution;
            return this;
        }

        public Builder rotatable(boolean rotatable) {
            this.rotatable = rotatable;
            return this;
        }

        public Builder maxRotationsPerPeriod(Integer maxRotationsPerPeriod) {
            this.maxRotationsPerPeriod = maxRotationsPerPeriod;
            return this;
        }

        public Builder distributionConfig(DistributionConfig distributionConfig) {
            this.distributionConfig = distributionConfig;
            return this;
        }

        public QuestGroupConfig build() {
            return new QuestGroupConfig(this);
        }
    }
}