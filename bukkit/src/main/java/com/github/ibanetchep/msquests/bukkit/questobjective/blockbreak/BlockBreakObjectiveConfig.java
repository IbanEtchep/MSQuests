package com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak;

import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    private final Material blockType;

    public BlockBreakObjectiveConfig(String key, Map<String, Object> config) {
        super(key, config);
        this.blockType = Material.valueOf((String) config.get("block_type"));
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "type", "block_break",
                "block_type", blockType.name(),
                "amount", amount
        );
    }

    public Material getBlockType() {
        return blockType;
    }
}
