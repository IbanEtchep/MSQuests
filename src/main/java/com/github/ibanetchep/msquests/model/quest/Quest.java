package com.github.ibanetchep.msquests.model.quest;

import com.github.ibanetchep.msquests.model.actor.QuestActor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Quest {

    private final UUID id;
    private QuestDefinition quest;
    private QuestStatus status;
    private QuestActor actor;
    private Map<UUID, QuestObjective<?>> objectives;
    private Date startedAt;
    private Date expiresAt;
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;

    public Quest(UUID uniqueId, QuestDefinition quest, QuestActor actor, QuestStatus status, Date startedAt, Date expiresAt, Date completedAt, Date createdAt, Date updatedAt) {
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

    public QuestDefinition getQuest() {
        return quest;
    }

    public void setQuest(QuestDefinition quest) {
        this.quest = quest;
    }

    public Map<UUID, QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public void setObjectives(Map<UUID, QuestObjective<?>> objectives) {
        this.objectives = objectives;
    }
}
