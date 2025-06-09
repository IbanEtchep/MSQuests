package com.github.ibanetchep.msquests.bukkit.questobjective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;

import java.util.UUID;

public class DeliverItemObjective extends QuestObjective<DeliverItemObjectiveConfig> {

    public DeliverItemObjective(UUID id, Quest quest, int progress, DeliverItemObjectiveConfig objectiveConfig) {
        super(id, quest, progress, objectiveConfig);
    }
}
