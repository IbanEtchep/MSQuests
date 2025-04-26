package com.github.ibanetchep.msquests.bukkit.questobjective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;

import java.util.Map;

public class DeliverItemObjectiveConfig extends QuestObjectiveConfig {

    public DeliverItemObjectiveConfig(String key, Map<String, Object> config) {
        super(key, config);
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }
}
