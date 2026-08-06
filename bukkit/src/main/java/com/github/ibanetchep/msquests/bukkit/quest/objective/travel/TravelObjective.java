package com.github.ibanetchep.msquests.bukkit.quest.objective.travel;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class TravelObjective extends AbstractQuestObjective<TravelObjectiveConfig> {

    public TravelObjective(QuestStage questStage, TravelObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, status);
    }
}
