package com.github.ibanetchep.msquests.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Quest {

    private UUID uniqueId;
    private String name;
    private String description;
    private Set<String> tags;
    private Map<UUID, QuestObjective> objectives;
    private long duration; // max in seconds
    private List<QuestReward> rewards;

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

    public Map<UUID, QuestObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(Map<UUID, QuestObjective> objectives) {
        this.objectives = objectives;
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

    public List<QuestReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<QuestReward> rewards) {
        this.rewards = rewards;
    }
}
