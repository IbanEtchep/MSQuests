package com.github.ibanetchep.msquests.core.event;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

/**
 * Every objective of a stage is completed. Fired after the objective that completed it, and before
 * the quest completes when it was the last stage.
 */
public class CoreQuestStageCompletedEvent extends CoreEvent {

    private final QuestStage stage;
    private final @Nullable PlayerProfile profile;

    public CoreQuestStageCompletedEvent(QuestStage stage, @Nullable PlayerProfile profile) {
        this.stage = stage;
        this.profile = profile;
    }

    public QuestStage getStage() {
        return stage;
    }

    public @Nullable PlayerProfile getPlayerProfile() {
        return profile;
    }
}
