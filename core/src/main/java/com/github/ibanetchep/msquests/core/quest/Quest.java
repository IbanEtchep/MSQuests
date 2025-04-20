package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Quest {

    private final UUID id;
    private QuestConfig quest;
    private QuestStatus status;
    private QuestActor actor;
    private Map<UUID, QuestObjective<?>> objectives;
    private Date startedAt;
    private Date expiresAt;
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;
    private QuestCategory category;

    public Quest(UUID uniqueId, QuestConfig quest, QuestActor actor, QuestStatus status, Date startedAt, Date expiresAt, Date completedAt, Date createdAt, Date updatedAt) {
        this.id = uniqueId;
        this.quest = quest;
        this.status = status;
        this.actor = actor;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public QuestActor getActor() {
        return actor;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public void setActor(QuestActor actor) {
        this.actor = actor;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public QuestConfig getQuest() {
        return quest;
    }

    public void setQuest(QuestConfig quest) {
        this.quest = quest;
    }

    public Map<UUID, QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public void addObjective(QuestObjective<?> objective) {
        this.objectives.put(objective.getId(), objective);
    }

    public QuestObjective<?> getObjective(UUID id) {
        return this.objectives.get(id);
    }

    public QuestCategory getCategory() {
        return category;
    }

    public void setCategory(QuestCategory category) {
        this.category = category;
    }
}
