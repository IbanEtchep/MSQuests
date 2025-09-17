package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "material")
    private Material material;

    @ConfigField(name = "amount", required = true)
    private int amount;

    public BlockBreakObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
    }

    @Override
    public int getTargetAmount() {
        return amount;
    }

    public Material getMaterial() {
        return material;
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
