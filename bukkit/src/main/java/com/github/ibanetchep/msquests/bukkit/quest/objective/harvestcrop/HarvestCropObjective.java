package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class HarvestCropObjective extends AbstractQuestObjective<HarvestCropObjectiveConfig> {

    public HarvestCropObjective(QuestStage questStage, HarvestCropObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
