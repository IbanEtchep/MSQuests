package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;

public class KillEntityObjective extends AbstractQuestObjective<KillEntityObjectiveConfig> {

    public KillEntityObjective(Quest quest, KillEntityObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, 0, objectiveConfig.getAmount());
    }
}
