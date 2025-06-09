package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.manager.QuestManager;

import java.util.List;
import java.util.UUID;


public abstract class QuestObjectiveHandler<T extends QuestObjective<?>> {

    protected QuestManager questManager;

    public QuestObjectiveHandler(QuestManager questManager) {
        this.questManager = questManager;
    }

    protected abstract String getObjectiveType();

    public List<T> getQuestObjectives(UUID playerId) {
        return questManager.getObjectivesByType(playerId, getObjectiveType());
    }

    /**
     * Updates the progress of the given objective.
     *
     * @param objective The objective to update
     * @param amount The amount to increment progress by
     */
    protected void updateProgress(T objective, int amount) {
        int newProgress = objective.getProgress() + amount;
        int target = objective.getObjectiveConfig().getTargetAmount();

        newProgress = Math.min(newProgress, target);

        objective.setProgress(newProgress);

        boolean completed = newProgress >= target;

    }
}
