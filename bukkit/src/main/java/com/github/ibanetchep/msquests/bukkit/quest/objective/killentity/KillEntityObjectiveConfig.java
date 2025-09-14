package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class KillEntityObjectiveConfig extends QuestObjectiveConfig {

    private final EntityType entityType;

    public KillEntityObjectiveConfig(String key, Map<String, Object> config) {
        super(key, config);
        this.entityType = EntityType.valueOf(config.get("entity_type").toString().toUpperCase());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "type", ObjectiveTypes.KILL_ENTITY,
                "entity_type", "<lang:" + entityType.translationKey() + ">",
                "amount", amount
        );
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
