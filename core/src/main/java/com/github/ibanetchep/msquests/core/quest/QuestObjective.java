package com.github.ibanetchep.msquests.core.quest;

import java.util.Date;
import java.util.UUID;

public abstract class QuestObjective<T extends QuestObjectiveConfig> {

    protected T objectiveConfig;

    protected int progress;
    protected QuestObjectiveStatus status = QuestObjectiveStatus.IN_PROGRESS;
    protected Quest quest;

    public QuestObjective(Quest quest,T objectiveConfig, int progress) {
        this.quest = quest;
        this.objectiveConfig = objectiveConfig;
        this.progress = progress;
    }

    public boolean isCompleted() {
        return progress >= objectiveConfig.getTargetAmount() || status == QuestObjectiveStatus.COMPLETED;
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

    public T getObjectiveConfig() {
        return objectiveConfig;
    }

    public String getType() {
        return objectiveConfig.getType();
    }
}