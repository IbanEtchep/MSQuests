package com.github.ibanetchep.msquests.model.quest.definition;

import com.github.ibanetchep.msquests.annotation.ObjectiveDefinitionType;
import com.github.ibanetchep.msquests.model.quest.QuestObjectiveDefinition;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveDefinitionType(type = "block_break")
public class BlockBreakObjectiveDefinition extends QuestObjectiveDefinition {

    private final Material blockType;
    private final int amountToBreak;

    public BlockBreakObjectiveDefinition(Map<String, String> config) {
        super(config);
        this.blockType = Material.valueOf(config.get("block_type"));
        this.amountToBreak = Integer.parseInt(config.get("amount_to_break"));
    }

    public Material getBlockType() {
        return blockType;
    }

    public int getAmountToBreak() {
        return amountToBreak;
    }
}
