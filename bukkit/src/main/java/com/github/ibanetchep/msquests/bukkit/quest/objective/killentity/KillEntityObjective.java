package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.progress.NumericProgressTracker;

public class KillEntityObjective extends AbstractQuestObjective<KillEntityObjectiveConfig, NumericProgressTracker> {

    public KillEntityObjective(Quest quest, KillEntityObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, new NumericProgressTracker(0, objectiveConfig.getAmount()));
    }
}
