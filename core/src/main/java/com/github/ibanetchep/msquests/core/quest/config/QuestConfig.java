package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import com.github.ibanetchep.msquests.core.quest.reward.Reward;

import java.util.*;

public class QuestConfig {

    private String key;
    private String name;
    private String description;
    private Set<String> tags;
    private final Map<String, QuestObjectiveConfig> objectives;
    private long duration; // max in seconds
    private List<Reward> rewards;
    private QuestGroup group;

    public QuestConfig(String key, String name, String description, long duration) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.tags = new HashSet<>();
        this.objectives = new HashMap<>();
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

    public Map<String, QuestObjectiveConfig> getObjectives() {
        return objectives;
    }

    public void addObjective(QuestObjectiveConfig objective) {
        objectives.put(objective.getKey(), objective);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
    }

    public void setGroup(QuestGroup group) {
        this.group = group;
    }

    public QuestGroup getGroup() {
        return group;
    }
}
