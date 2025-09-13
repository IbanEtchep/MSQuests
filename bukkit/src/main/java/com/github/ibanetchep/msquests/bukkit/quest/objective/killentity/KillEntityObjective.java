package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class KillEntityObjective extends QuestObjective<KillEntityObjectiveConfig> {

    public KillEntityObjective(Quest quest, KillEntityObjectiveConfig objectiveConfig, int progress) {
        super(quest, objectiveConfig, progress);
    }
}
