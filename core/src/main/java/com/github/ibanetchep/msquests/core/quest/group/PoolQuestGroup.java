package com.github.ibanetchep.msquests.core.quest.group;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class PoolQuestGroup extends QuestGroup {

    @Nullable
    private final String periodSwitchCron;
    @Nullable
    protected Integer maxActiveQuests = 1;
    @Nullable
    protected Integer maxPerPeriod = 1;

    public PoolQuestGroup(
            String key,
            String name,
            String description,
            Instant startAt,
            Instant endAt,
            @Nullable String periodSwitchCron,
            @Nullable Integer maxActiveQuests,
            @Nullable Integer maxPerPeriod
    ) {
        super(key, name, description, startAt, endAt);
        this.periodSwitchCron = periodSwitchCron;
        this.maxActiveQuests = maxActiveQuests;
        this.maxPerPeriod = maxPerPeriod;
    }

    public @Nullable Integer getMaxActiveQuests() {
        return maxActiveQuests;
    }

    public @Nullable Integer getMaxPerPeriod() {
        return maxPerPeriod;
    }

    public @Nullable String getPeriodSwitchCron() {
        return periodSwitchCron;
    }

    @Override
    public QuestGroupType getType() {
        return QuestGroupType.POOL;
    }

    @Override
    public QuestConfig getNextQuest(QuestConfig current) {
        return null;
    }
}
