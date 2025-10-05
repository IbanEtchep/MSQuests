package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;

public class BlockBreakObjective extends AbstractQuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(Quest quest, BlockBreakObjectiveConfig objectiveConfig, int progress) {
        super(quest, objectiveConfig, progress, objectiveConfig.getAmount());
    }
}
