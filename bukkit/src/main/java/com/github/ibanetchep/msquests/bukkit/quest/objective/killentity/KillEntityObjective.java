package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class KillEntityObjective extends QuestObjective<KillEntityObjectiveConfig> {

    public KillEntityObjective(UUID id, Quest quest, int progress, KillEntityObjectiveConfig objectiveConfig) {
        super(id, quest, progress, objectiveConfig);
    }
}
