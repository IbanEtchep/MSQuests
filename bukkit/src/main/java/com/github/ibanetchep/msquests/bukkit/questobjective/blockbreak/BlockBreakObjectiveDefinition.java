package com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveDefinition;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(type = "block_break")
public class BlockBreakObjectiveDefinition extends QuestObjectiveDefinition {

    private final Material blockType;

    public BlockBreakObjectiveDefinition(Map<String, String> config) {
        super(config);
        this.blockType = Material.valueOf(config.get("block_type"));
    }

    public Material getBlockType() {
        return blockType;
    }
}
