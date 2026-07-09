package com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal;

import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;

public class BreedAnimalObjective extends AbstractQuestObjective<BreedAnimalObjectiveConfig> {

    public BreedAnimalObjective(QuestStage questStage, BreedAnimalObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, objectiveConfig.getAmount(), status);
    }
}
