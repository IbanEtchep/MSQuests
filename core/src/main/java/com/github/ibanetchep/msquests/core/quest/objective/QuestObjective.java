package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

public interface QuestObjective extends PlaceholderProvider {

    Quest getQuest();
    QuestObjectiveConfig getObjectiveConfig();
    boolean isCompleted();
    double getProgressPercent();
    QuestObjectiveStatus getStatus();
    String progressToJson();
    void updateProgressFromJson(String json);


    default String getType() {
        return getObjectiveConfig().getType();
    }
}
