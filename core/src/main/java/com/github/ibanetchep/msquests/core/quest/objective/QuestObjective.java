package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.objective.progress.ProgressTracker;

public interface QuestObjective {

    Quest getQuest();
    QuestObjectiveConfig getObjectiveConfig();
    ProgressTracker getProgressTracker();
    boolean isCompleted();
    double getProgressPercent();
    QuestObjectiveStatus getStatus();
    QuestObjectiveDTO toDTO();

    default String getType() {
        return getObjectiveConfig().getType();
    }

}
