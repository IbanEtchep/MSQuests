package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.CounterQuestObjective;

public class BlockBreakObjective extends CounterQuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(Quest quest, BlockBreakObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, 0, objectiveConfig.getAmount());
    }
}
