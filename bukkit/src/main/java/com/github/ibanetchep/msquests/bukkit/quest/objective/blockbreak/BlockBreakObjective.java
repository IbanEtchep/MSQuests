package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class BlockBreakObjective extends QuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(UUID id, Quest quest, int progress, BlockBreakObjectiveConfig objectiveConfig) {
        super(id, quest, progress, objectiveConfig);
    }
}
