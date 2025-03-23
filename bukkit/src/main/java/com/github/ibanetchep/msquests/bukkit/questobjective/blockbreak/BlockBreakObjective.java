package com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class BlockBreakObjective extends QuestObjective<BlockBreakObjectiveDefinition> {

    public BlockBreakObjective(UUID id, Quest quest, int progress, BlockBreakObjectiveDefinition objectiveDefinition) {
        super(id, quest, progress, objectiveDefinition);
    }
}
