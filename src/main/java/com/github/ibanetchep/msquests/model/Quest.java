package com.github.ibanetchep.msquests.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Quest {

    private UUID uniqueId;
    private String name;
    private String description;
    private Category category;
    private Map<UUID, QuestObjective> objectives;
    private List<QuestReward> rewards;
    private long duration;

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Map<UUID, QuestObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(Map<UUID, QuestObjective> objectives) {
        this.objectives = objectives;
    }

    public List<QuestReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<QuestReward> rewards) {
        this.rewards = rewards;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
