package com.github.ibanetchep.msquests.core.quest.config.group;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
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

public class QuestGroupConfig {

    private final String key;
    private final String name;
    private final String description;
    private final String actorType;

    private final Map<String, QuestConfig> questConfigs;
    private final List<QuestConfig> orderedQuests;

    private final @Nullable Instant startAt;
    private final @Nullable Instant endAt;

    private final QuestDistributionMode distributionMode;
    private final @Nullable String resetCron;
    private final @Nullable Integer maxPerPeriod;
    private final @Nullable Integer maxActive;
    private final boolean repeatable;

    private final List<QuestAction> questStartActions;
    private final List<QuestAction> questCompleteActions;
    private final List<QuestAction> objectiveProgressActions;
    private final List<QuestAction> objectiveCompleteActions;

    private QuestGroupConfig(Builder builder) {
        this.key = builder.key;
        this.name = builder.name;
        this.description = builder.description;
        this.repeatable = builder.repeatable;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.distributionMode = builder.distributionMode;
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

    public QuestDistributionMode getDistributionMode() {
        return distributionMode;
    }

    public @Nullable String getResetCron() {
        return resetCron;
    }

    public @Nullable Integer getMaxPerPeriod() {
        return maxPerPeriod;
    }

    public @Nullable Integer getMaxActive() {
        return maxActive;
    }

    public boolean isRepeatable() {
        return repeatable;
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

    public static class Builder {
        private final String key;
        private final String name;
        private final String description;
        private final String actorType;

        private @Nullable Instant startAt;
        private @Nullable Instant endAt;
        private QuestDistributionMode distributionMode = QuestDistributionMode.SEQUENTIAL;
        private @Nullable String resetCron;
        private @Nullable Integer maxPerPeriod = 1;
        private @Nullable Integer maxActive = 1;
        private boolean repeatable = false;

        private List<QuestAction> questStartActions;
        private List<QuestAction> questCompleteActions;
        private List<QuestAction> objectiveProgressActions;
        private List<QuestAction> objectiveCompleteActions;

        public Builder(String key, String name, String description, String actorType) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.actorType = actorType;
            this.questStartActions = new ArrayList<>();
            this.questCompleteActions = new ArrayList<>();
            this.objectiveProgressActions = new ArrayList<>();
            this.objectiveCompleteActions = new ArrayList<>();
        }

        public Builder startAt(Instant startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder endAt(Instant endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder distributionMode(QuestDistributionMode distributionMode) {
            this.distributionMode = distributionMode;
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

        public Builder repeatable(boolean repeatable) {
            this.repeatable = repeatable;
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

        public QuestGroupConfig build() {
            return new QuestGroupConfig(this);
        }
    }
}