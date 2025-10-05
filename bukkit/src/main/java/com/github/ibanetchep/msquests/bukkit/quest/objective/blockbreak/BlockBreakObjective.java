package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;

public class BlockBreakObjective extends AbstractQuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(QuestStage questStage, BlockBreakObjectiveConfig objectiveConfig, int progress) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount());
    }
}
