package com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class PlaceholderObjective extends AbstractQuestObjective<PlaceholderObjectiveConfig> {

    public PlaceholderObjective(QuestStage questStage, PlaceholderObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, status);
    }
}
