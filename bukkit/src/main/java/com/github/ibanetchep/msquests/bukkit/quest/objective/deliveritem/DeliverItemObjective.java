package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;

public class DeliverItemObjective extends AbstractQuestObjective<DeliverItemObjectiveConfig> {

    public DeliverItemObjective(QuestStage questStage, DeliverItemObjectiveConfig objectiveConfig, int progress) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount());
    }
}
