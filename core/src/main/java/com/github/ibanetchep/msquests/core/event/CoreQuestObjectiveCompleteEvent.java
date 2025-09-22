package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

public class CoreQuestObjectiveCompleteEvent extends CoreEvent {

    private final QuestObjective objective;
    private final @Nullable PlayerProfile profile;

    public CoreQuestObjectiveCompleteEvent(QuestObjective objective, @Nullable PlayerProfile profile) {
        this.objective = objective;
        this.profile = profile;
    }

    public QuestObjective getObjective() {
        return objective;
    }

    public @Nullable PlayerProfile getPlayerProfile() {
        return profile;
    }
}
