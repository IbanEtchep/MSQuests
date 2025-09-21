package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;


public abstract class QuestObjectiveHandler<T extends QuestObjective<?>> {

    protected MSQuestsPlatform platform;

    public QuestObjectiveHandler(MSQuestsPlatform platform) {
        this.platform = platform;
    }

    protected abstract String getObjectiveType();

    protected PlayerProfile getPlayerProfile(UUID playerId) {
        return platform.getPlayerProfileRegistry().getPlayerProfile(playerId);
    }

    public List<T> getQuestObjectives(PlayerProfile profile) {
        return profile.getActiveObjectivesByType(getObjectiveType());
    }

    /**
     * Updates the progress of the given objective.
     *
     * @param objective The objective to update
     * @param amount The amount to increment progress by
     */
    protected void updateProgress(T objective, int amount, @Nullable PlayerProfile profile) {
        platform.getQuestLifecycleService().updateObjectiveProgress(objective, amount, profile);
    }
}
