package com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class CraftItemObjective extends AbstractQuestObjective<CraftItemObjectiveConfig> {

    public CraftItemObjective(QuestStage questStage, CraftItemObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, status);
    }
}
