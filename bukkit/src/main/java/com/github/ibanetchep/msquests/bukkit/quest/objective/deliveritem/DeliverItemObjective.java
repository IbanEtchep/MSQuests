package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.CounterQuestObjective;

public class DeliverItemObjective extends CounterQuestObjective<DeliverItemObjectiveConfig> {

    public DeliverItemObjective(Quest quest, DeliverItemObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, 0, objectiveConfig.getAmount());
    }
}
