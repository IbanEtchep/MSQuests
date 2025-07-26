package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    private final Material material;

    public BlockBreakObjectiveConfig(String key, Map<String, Object> config) {
        super(key, config);
        this.material = Material.valueOf(config.get("material").toString().toUpperCase());
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "type", ObjectiveTypes.BLOCK_BREAK,
                "material", material.name(),
                "amount", amount
        );
    }

    public Material getMaterial() {
        return material;
    }
}
