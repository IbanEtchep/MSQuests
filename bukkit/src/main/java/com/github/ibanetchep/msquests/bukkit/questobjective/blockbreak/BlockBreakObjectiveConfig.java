package com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(type = "block_break")
public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    private final Material blockType;

    public BlockBreakObjectiveConfig(String key, Map<String, String> config) {
        super(key, config);
        this.blockType = Material.valueOf(config.get("block_type"));
    }

    public Material getBlockType() {
        return blockType;
    }
}
