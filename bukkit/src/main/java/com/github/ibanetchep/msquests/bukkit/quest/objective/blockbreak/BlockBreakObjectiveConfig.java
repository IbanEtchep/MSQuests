package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.BLOCK_BREAK)
public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "material")
    private Material material;

    @ConfigField(name = "amount", required = true)
    private int amount;

    public BlockBreakObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "material", "<lang:" + material.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "material", material.name(),
                        "amount", amount
                )
        );
    }
}
