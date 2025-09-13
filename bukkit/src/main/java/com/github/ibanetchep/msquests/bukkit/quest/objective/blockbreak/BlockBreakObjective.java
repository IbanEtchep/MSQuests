package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class BlockBreakObjective extends QuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(Quest quest, BlockBreakObjectiveConfig objectiveConfig, int progress) {
        super(quest, objectiveConfig, progress);
    }

}
