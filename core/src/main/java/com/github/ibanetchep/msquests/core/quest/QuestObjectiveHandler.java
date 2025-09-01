package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;

import java.util.List;
import java.util.UUID;


public abstract class QuestObjectiveHandler<T extends QuestObjective<?>> {

    protected MSQuestsPlatform platform;

    public QuestObjectiveHandler(MSQuestsPlatform platform) {
        this.platform = platform;
    }

    protected abstract String getObjectiveType();

    public List<T> getQuestObjectives(UUID playerId) {
        return platform.getQuestRegistry().getObjectivesByType(playerId, getObjectiveType());
    }

    /**
     * Updates the progress of the given objective.
     *
     * @param objective The objective to update
     * @param amount The amount to increment progress by
     */
    protected void updateProgress(T objective, int amount) {
        platform.getQuestLifecycleService().updateObjectiveProgress(objective, amount);
    }
}
