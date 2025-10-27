package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class BlockBreakObjective extends AbstractQuestObjective<BlockBreakObjectiveConfig> {

    public BlockBreakObjective(QuestStage questStage, BlockBreakObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
