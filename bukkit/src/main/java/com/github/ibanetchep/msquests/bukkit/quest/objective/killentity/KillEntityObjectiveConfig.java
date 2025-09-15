package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class KillEntityObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "entity_type")
    private EntityType entityType;

    @ConfigField(name = "amount", required = true)
    private int amount;

    public KillEntityObjectiveConfig(String key, String type, Map<String, Object> config) {
        super(key, type, config);
    }

    @Override
    public int getTargetAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "entity_type", "<lang:" + entityType.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
