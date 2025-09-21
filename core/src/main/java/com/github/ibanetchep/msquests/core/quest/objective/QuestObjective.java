package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Map;

public abstract class QuestObjective<T extends QuestObjectiveConfig> implements Translatable, PlaceholderProvider {

    protected T objectiveConfig;

    protected int progress;
    protected Quest quest;

    public QuestObjective(Quest quest,T objectiveConfig, int progress) {
        this.quest = quest;
        this.objectiveConfig = objectiveConfig;
        this.progress = progress;
    }

    public boolean isCompleted() {
        return progress >= objectiveConfig.getTargetAmount();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgressPercent() {
        if (objectiveConfig.getTargetAmount() == 0 || getStatus() == QuestObjectiveStatus.COMPLETED) {
            return 100;
        }

        return (int) ((progress / (float) objectiveConfig.getTargetAmount()) * 100);
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestObjectiveStatus getStatus() {
        if (isCompleted()) {
            return QuestObjectiveStatus.COMPLETED;
        }

        if(quest.getQuestConfig().getFlow() == Flow.SEQUENTIAL) {
            QuestObjective<?> firstActiveObjective = quest.getFirstActiveObjective();
            return firstActiveObjective == this ? QuestObjectiveStatus.IN_PROGRESS : QuestObjectiveStatus.PENDING;
        }

        return QuestObjectiveStatus.IN_PROGRESS;
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