package com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class ExecuteCommandObjective extends AbstractQuestObjective<ExecuteCommandObjectiveConfig> {

    public ExecuteCommandObjective(QuestStage questStage, ExecuteCommandObjectiveConfig objectiveConfig, int progress, int target, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, target, status);
    }
}
