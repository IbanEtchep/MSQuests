package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Map;

public abstract class QuestObjective<T extends QuestObjectiveConfig> implements Translatable, PlaceholderProvider {

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

    public int getPercentage() {
        return (int) ((progress / (float) objectiveConfig.getTargetAmount()) * 100);
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

    @Override
    public String getTranslationKey() {
        return objectiveConfig.getTranslationKey();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return objectiveConfig.getPlaceholders();
    }
}