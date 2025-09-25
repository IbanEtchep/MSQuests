package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public abstract class AbstractQuestObjective<C extends QuestObjectiveConfig, P> implements QuestObjective {

    protected C objectiveConfig;
    protected final AtomicReference<P> progress;
    protected Quest quest;

    public AbstractQuestObjective(Quest quest, C objectiveConfig, P progressTracker) {
        this.quest = quest;
        this.objectiveConfig = objectiveConfig;
        this.progress = new AtomicReference<>(progressTracker);
    }

    public P getProgress() {
        return progress.get();
    }

    public void updateProgress(UnaryOperator<P> update) {
        progress.updateAndGet(update);
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
}