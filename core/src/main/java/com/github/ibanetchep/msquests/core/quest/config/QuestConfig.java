package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.*;

public class QuestConfig {

    private String key;
    private String name;
    private String description;
    private final Map<String, QuestStageConfig> stages;
    private final List<QuestAction> rewards;
    private long duration; // max in seconds
    private QuestGroupConfig group;

    public QuestConfig(String key, String name, String description, long duration) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.stages = new LinkedHashMap<>();
        this.rewards = new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public List<QuestAction> getRewards() {
        return rewards;
    }

    public void addReward(QuestAction questAction) {
        rewards.add(questAction);
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
}
