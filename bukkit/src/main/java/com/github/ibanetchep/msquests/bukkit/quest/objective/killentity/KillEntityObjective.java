package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.CounterQuestObjective;

public class KillEntityObjective extends CounterQuestObjective<KillEntityObjectiveConfig> {

    public KillEntityObjective(Quest quest, KillEntityObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, 0, objectiveConfig.getAmount());
    }
}
