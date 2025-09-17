package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

@AtLeastOneOfFields({"material", "itemKey"})
public class DeliverItemObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "material")
    private Material material;

    @ConfigField(name = "itemKey")
    private String itemKey;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public DeliverItemObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "amount", amount,
                        "material", material.name(),
                        "itemKey", itemKey
                )
        );
    }

    @Override
    public int getTargetAmount() {
        return amount;
    }

    public Material getMaterial() {
        return material;
    }

    public String getItemKey() {
        return itemKey;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "item_key", itemKey,
                "material", "<lang:" + material.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }
}
