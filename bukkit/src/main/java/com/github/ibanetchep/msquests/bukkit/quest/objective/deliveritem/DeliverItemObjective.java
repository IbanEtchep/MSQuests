package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.progress.NumericProgressTracker;

public class DeliverItemObjective extends AbstractQuestObjective<DeliverItemObjectiveConfig, NumericProgressTracker> {

    public DeliverItemObjective(Quest quest, DeliverItemObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, new NumericProgressTracker(0, objectiveConfig.getAmount()));
    }
}
