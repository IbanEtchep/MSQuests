package com.github.ibanetchep.msquests.bukkit.quest.objective.fishing;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class FishingObjective extends AbstractQuestObjective<FishingObjectiveConfig> {

    public FishingObjective(QuestStage questStage, FishingObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
