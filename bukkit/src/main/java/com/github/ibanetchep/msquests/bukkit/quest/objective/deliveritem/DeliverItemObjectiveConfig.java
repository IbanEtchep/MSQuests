package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.DELIVER_ITEM)
@AtLeastOneOfFields({"material", "itemKey"})
public class DeliverItemObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "material")
    private Material material;

    @ConfigField(name = "itemKey")
    private String itemKey;

    @ConfigField(name = "amount")
    private int amount = 1;

    public DeliverItemObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);

        if(dto.params().containsKey("material")) {
            material = Material.valueOf(dto.params().get("material").toString().toUpperCase());
        }

        if(dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }

        if(dto.params().containsKey("itemKey")) {
            itemKey = dto.params().get("itemKey").toString();
        }
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

    public int getAmount() {
        return amount;
    }

    public Material getMaterial() {
        return material;
    }

    public String getItemKey() {
        return itemKey;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "item_key", itemKey,
                "material", "<lang:" + material.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }
}
