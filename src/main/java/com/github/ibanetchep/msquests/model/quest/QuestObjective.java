package com.github.ibanetchep.msquests.model.quest;

import java.util.Date;
import java.util.UUID;

public abstract class QuestObjective<T extends QuestObjectiveDefinition> {

    protected T objectiveDefinition;

    protected UUID id;
    protected int progress;
    protected QuestObjectiveStatus status = QuestObjectiveStatus.IN_PROGRESS;
    protected Quest quest;

    protected Date startedAt;
    protected Date completedAt;
    protected Date createdAt;
    protected Date updatedAt;

    public QuestObjective(UUID id, Quest quest, int progress, T objectiveDefinition) {
        this.id = id;
        this.quest = quest;
        this.progress = progress;
        this.objectiveDefinition = objectiveDefinition;
    }

    public abstract boolean isCompleted();

    public UUID getId() {
        return id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestObjectiveStatus getStatus() {
        return status;
    }

    public void setStatus(QuestObjectiveStatus status) {
        this.status = status;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public T getObjectiveDefinition() {
        return objectiveDefinition;
    }

    public void callOnProgress() {
        //todo call custom bukkit event
    }

    public void callOnComplete() {
        //todo call custom bukkit event
    }
}