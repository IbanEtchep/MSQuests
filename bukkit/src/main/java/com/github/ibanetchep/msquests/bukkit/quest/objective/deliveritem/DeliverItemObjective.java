package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;

public class DeliverItemObjective extends AbstractQuestObjective<DeliverItemObjectiveConfig> {

    public DeliverItemObjective(Quest quest, DeliverItemObjectiveConfig objectiveConfig) {
        super(quest, objectiveConfig, 0, objectiveConfig.getAmount());
    }
}
