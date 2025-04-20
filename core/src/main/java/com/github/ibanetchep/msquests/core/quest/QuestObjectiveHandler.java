package com.github.ibanetchep.msquests.core.quest;

import java.util.List;


public abstract class QuestObjectiveHandler<T extends QuestObjective<?>> {

    public abstract String getType();

    public List<T> getQuestObjectives() {
        throw new UnsupportedOperationException("Not supported yet.");
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
