package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class KillEntityObjective extends AbstractQuestObjective<KillEntityObjectiveConfig> {

    public KillEntityObjective(QuestStage questStage, KillEntityObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
