package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStage;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

public interface QuestObjective extends PlaceholderProvider, Translatable {

    QuestStage getStage();
    QuestObjectiveConfig getObjectiveConfig();
    boolean isCompleted();
    void complete();
    void incrementProgress(int progress);
    double getProgressPercent();
    QuestObjectiveStatus getStatus();
    int getTarget();
    int getProgress();

    default String getType() {
        return getObjectiveConfig().getType();
    }

    default Quest getQuest() {
        return getStage().getQuest();
    }
}
