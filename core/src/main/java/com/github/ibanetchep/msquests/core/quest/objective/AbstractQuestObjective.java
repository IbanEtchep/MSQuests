package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.objective.progress.ProgressTracker;

public abstract class AbstractQuestObjective<C extends QuestObjectiveConfig, P extends ProgressTracker> implements QuestObjective {

    protected C objectiveConfig;
    protected P progressTracker;
    protected Quest quest;

    public AbstractQuestObjective(Quest quest, C objectiveConfig, P progressTracker) {
        this.quest = quest;
        this.objectiveConfig = objectiveConfig;
        this.progressTracker = progressTracker;
    }

    public P getProgressTracker() {
        return progressTracker;
    }

    public boolean isCompleted() {
        return progressTracker.isCompleted();
    }

    public double getProgressPercent() {
        if (getStatus() == QuestObjectiveStatus.COMPLETED) {
            return 100;
        }

        return progressTracker.getProgressPercent();
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestObjectiveStatus getStatus() {
        if (isCompleted()) {
            return QuestObjectiveStatus.COMPLETED;
        }

        if(quest.getQuestConfig().getFlow() == Flow.SEQUENTIAL) {
            QuestObjective firstActiveObjective = quest.getFirstActiveObjective();
            return firstActiveObjective == this ? QuestObjectiveStatus.IN_PROGRESS : QuestObjectiveStatus.PENDING;
        }

        return QuestObjectiveStatus.IN_PROGRESS;
    }

    public C getObjectiveConfig() {
        return objectiveConfig;
    }

    public QuestObjectiveDTO toDTO() {
        return new QuestObjectiveDTO(
                quest.getId(),
                objectiveConfig.getKey(),
                getStatus(),
                progressTracker.toJson()
        );
    }
}