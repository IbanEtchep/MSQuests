package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.progress.NumericProgressTracker;

public class BlockBreakObjective extends AbstractQuestObjective<BlockBreakObjectiveConfig, NumericProgressTracker> {

    public BlockBreakObjective(Quest quest, BlockBreakObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, new NumericProgressTracker(0, objectiveConfig.getAmount()));
    }

}
