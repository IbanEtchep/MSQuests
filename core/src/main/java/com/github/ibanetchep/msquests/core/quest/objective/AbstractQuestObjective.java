package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractQuestObjective<C extends QuestObjectiveConfig> implements QuestObjective {

    protected C objectiveConfig;
    protected final AtomicInteger progress;
    protected int target;
    protected Quest quest;

    public AbstractQuestObjective(Quest quest, C objectiveConfig, int progress, int target) {
        this.quest = quest;
        this.objectiveConfig = objectiveConfig;
        this.progress = new AtomicInteger(progress);
        this.target = target;
    }

    public int getProgress() {
        return progress.get();
    }

    public void incrementProgress(int progress) {
        this.progress.addAndGet(progress);
    }

    public void setProgress(int progress) {
        this.progress.set(progress);
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

    @Override
    public void complete() {
        incrementProgress(target - getProgress());
    }

    @Override
    public boolean isCompleted() {
        return getProgress() >= target;
    }

    public double getProgressPercent() {
        return (double) getProgress() / target;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        double percent = getProgressPercent() * 100; // en pourcentage
        String percentStr;

        if (percent == (int) percent) {
            percentStr = String.valueOf((int) percent); // entier
        } else {
            percentStr = String.format("%.1f", percent); // 1 chiffre après la virgule
        }

        return Map.of(
                "objective_progress", String.valueOf(getProgress()),
                "objective_target", String.valueOf(target),
                "objective_progress_percent", percentStr
        );
    }

    @Override
    public String getTranslationKey() {
        return "objective.progress.default";
    }

    @Override
    public int getTarget() {
        return target;
    }
}