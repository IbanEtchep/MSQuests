package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.BLOCK_BREAK)
public class BlockBreakObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "material")
    private @Nullable Material material;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public BlockBreakObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if(dto.params().containsKey("material")) {
            material = Material.valueOf(dto.params().get("material").toString().toUpperCase());
        }

        if(dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "material", material != null ? "<lang:" + material.translationKey() + ">" : "",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "material", material != null ? material.name() : null,
                        "amount", amount
                )
        );
    }
}
