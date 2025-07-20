package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.UUID;

public class DeliverItemObjective extends QuestObjective<DeliverItemObjectiveConfig> {

    public DeliverItemObjective(UUID id, Quest quest, int progress, DeliverItemObjectiveConfig objectiveConfig) {
        super(id, quest, progress, objectiveConfig);
    }
}
