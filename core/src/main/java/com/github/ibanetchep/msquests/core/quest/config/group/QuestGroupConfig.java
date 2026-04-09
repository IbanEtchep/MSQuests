package com.github.ibanetchep.msquests.core.quest.config.group;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestGroupConfig implements PlaceholderProvider {

    private final String key;
    private final String name;
    private final String description;
    private final String actorType;

    private final Map<String, QuestConfig> questConfigs;
    private final List<QuestConfig> orderedQuests;

    private final @Nullable Instant startAt;
    private final @Nullable Instant endAt;

    private final @Nullable String resetCron;
    private final @Nullable Integer maxPerPeriod;
    private final int maxActive;

    private final List<QuestAction> questStartActions;
    private final List<QuestAction> questCompleteActions;
    private final List<QuestAction> objectiveProgressActions;
    private final List<QuestAction> objectiveCompleteActions;
    private final List<QuestAction> questDistributionActions;
    private final List<QuestAction> actorLoadActions;
    private final List<QuestAction> allPeriodQuestsCompleteActions;

    private final Map<String, QuestTierConfig> tiers;
    private final @Nullable Map<String, Integer> tierDistribution;

    private final boolean rotatable;
    private final @Nullable Integer maxRotationsPerPeriod;

    private final @Nullable DistributionConfig distributionConfig;

    private QuestGroupConfig(Builder builder) {
        this.key = builder.key;
        this.name = builder.name;
        this.description = builder.description;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.resetCron = builder.resetCron;
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
        this.tiers = builder.tiers != null ? builder.tiers : Map.of();
        this.tierDistribution = builder.tierDistribution;
        this.rotatable = builder.rotatable;
        this.maxRotationsPerPeriod = builder.maxRotationsPerPeriod;
        this.distributionConfig = builder.distributionConfig;
    }

    public String getKey() {
        return key;
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

    public @Nullable Integer getMaxPerPeriod() {
        return maxPerPeriod;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public List<QuestAction> getQuestStartActions() {
        return Collections.unmodifiableList(questStartActions);
    }

    public List<QuestAction> getQuestCompleteActions() {
        return Collections.unmodifiableList(questCompleteActions);
    }

    public List<QuestAction> getObjectiveProgressActions() {
        return Collections.unmodifiableList(objectiveProgressActions);
    }

    public List<QuestAction> getObjectiveCompleteActions() {
        return Collections.unmodifiableList(objectiveCompleteActions);
    }

    public List<QuestAction> getQuestDistributionActions() {
        return Collections.unmodifiableList(questDistributionActions);
    }

    public List<QuestAction> getActorLoadActions() {
        return Collections.unmodifiableList(actorLoadActions);
    }

    public List<QuestAction> getAllPeriodQuestsCompleteActions() {
        return Collections.unmodifiableList(allPeriodQuestsCompleteActions);
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

        return CronUtils.getNextExecution(resetCron, Instant.now());
    }

    public @Nullable Instant getPreviousReset() {
        if (resetCron == null) {
            return  null;
        }

        return CronUtils.getPreviousExecution(resetCron, Instant.now());
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
        return Map.of(
                "group_key", key,
                "group_name", name,
                "group_description", description
        );
    }

    public static class Builder {
        private final String key;
        private final String name;
        private final String description;
        private final String actorType;

        private @Nullable Instant startAt;
        private @Nullable Instant endAt;
        private @Nullable String resetCron;
        private @Nullable Integer maxPerPeriod = 1;
        private int maxActive = 1;

        private List<QuestAction> questStartActions;
        private List<QuestAction> questCompleteActions;
        private List<QuestAction> objectiveProgressActions;
        private List<QuestAction> objectiveCompleteActions;
        private List<QuestAction> questDistributionActions;
        private List<QuestAction> actorLoadActions;
        private List<QuestAction> allPeriodQuestsCompleteActions;

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
            this.questStartActions = new ArrayList<>();
            this.questCompleteActions = new ArrayList<>();
            this.objectiveProgressActions = new ArrayList<>();
            this.objectiveCompleteActions = new ArrayList<>();
            this.questDistributionActions = new ArrayList<>();
            this.actorLoadActions = new ArrayList<>();
            this.allPeriodQuestsCompleteActions = new ArrayList<>();
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

        public  Builder questStartActions(List<QuestAction> questStartActions) {
            this.questStartActions = questStartActions;
            return this;
        }

        public Builder questCompleteActions(List<QuestAction> questCompleteActions) {
            this.questCompleteActions = questCompleteActions;
            return this;
        }

        public Builder objectiveProgressActions(List<QuestAction> objectiveProgressActions) {
            this.objectiveProgressActions = objectiveProgressActions;
            return this;
        }

        public Builder objectiveCompleteActions(List<QuestAction> objectiveCompleteActions) {
            this.objectiveCompleteActions = objectiveCompleteActions;
            return this;
        }

        public Builder questDistributionActions(List<QuestAction> questDistributionActions) {
            this.questDistributionActions = questDistributionActions;
            return this;
        }

        public Builder actorLoadActions(List<QuestAction> actorLoadActions) {
            this.actorLoadActions = actorLoadActions;
            return this;
        }

        public Builder allPeriodQuestsCompleteActions(List<QuestAction> allPeriodQuestsCompleteActions) {
            this.allPeriodQuestsCompleteActions = allPeriodQuestsCompleteActions;
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