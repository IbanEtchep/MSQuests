package com.github.ibanetchep.msquests.model.quest;

import java.util.*;

public class QuestDefinition {

    private UUID id;
    private String name;
    private String description;
    private Set<String> tags;
    private Map<UUID, QuestObjectiveDefinition> objectives;
    private long duration; // max in seconds
    private List<QuestReward> rewards;
    private Date createdAt;
    private Date updatedAt;

    public QuestDefinition(UUID id, String name, String description, long duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Map<UUID, QuestObjectiveDefinition> getObjectives() {
        return objectives;
    }

    public void setObjectives(Map<UUID, QuestObjectiveDefinition> objectives) {
        this.objectives = objectives;
    }

    public long getDuration() {
        return duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
