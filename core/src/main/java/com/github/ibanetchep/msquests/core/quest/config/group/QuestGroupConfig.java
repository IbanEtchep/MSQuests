package com.github.ibanetchep.msquests.core.quest.config.group;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestGroupConfig {

    private final String key;
    private final String name;
    private final String description;

    private final Map<String, QuestConfig> questConfigs;
    private final List<QuestConfig> orderedQuests;

    private final @Nullable Instant startAt;
    private final @Nullable Instant endAt;

    private final QuestDistributionMode distributionMode;
    private final @Nullable String resetCron;
    private final @Nullable Integer maxPerPeriod;
    private final @Nullable Integer maxActive;

    private QuestGroupConfig(Builder builder) {
        this.key = builder.key;
        this.name = builder.name;
        this.description = builder.description;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.distributionMode = builder.distributionMode;
        this.resetCron = builder.resetCron;
        this.maxPerPeriod = builder.maxPerPeriod;
        this.maxActive = builder.maxActive;
        this.questConfigs = new ConcurrentHashMap<>();
        this.orderedQuests = new CopyOnWriteArrayList<>();
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

    public @Nullable Instant getNextReset() {
        if(resetCron == null) {
            return null;
        }

        return CronUtils.getNextExecution(resetCron, Instant.now());
    }

    public @Nullable Instant getNextQuestsExpiration() {
        if(endAt != null) {
            return endAt;
        }

        return getNextReset();
    }

    public static class Builder {
        private final String key;
        private final String name;
        private final String description;

        private @Nullable Instant startAt;
        private @Nullable Instant endAt;
        private QuestDistributionMode distributionMode = QuestDistributionMode.SEQUENTIAL;
        private @Nullable String resetCron;
        private @Nullable Integer maxPerPeriod = 1;
        private @Nullable Integer maxActive = 1;

        public Builder(String key, String name, String description) {
            this.key = key;
            this.name = name;
            this.description = description;
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

        public QuestGroupConfig build() {
            return new QuestGroupConfig(this);
        }
    }
}