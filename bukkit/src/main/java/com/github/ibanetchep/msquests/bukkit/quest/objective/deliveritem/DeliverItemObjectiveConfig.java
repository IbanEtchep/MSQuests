package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemTarget;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;

import java.util.LinkedHashMap;
import java.util.Map;

@ObjectiveType(ObjectiveTypes.DELIVER_ITEM)
@AtLeastOneOfFields({"item", "material", "itemKey"})
public class DeliverItemObjectiveConfig extends QuestObjectiveConfig {

    /** {@code item} + {@code match}, or the legacy {@code material}. */
    @ConfigField(name = "item")
    private final ItemTarget item;

    @ConfigField(name = "itemKey")
    private String itemKey;

    @ConfigField(name = "amount")
    private int amount = 1;

    public DeliverItemObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        item = ItemTarget.read(dto.params(), "material");

        if(dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }

        if(dto.params().containsKey("itemKey")) {
            itemKey = dto.params().get("itemKey").toString();
        }
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        Map<String, Object> params = new LinkedHashMap<>();
        item.write(params);
        params.put("amount", amount);
        if (itemKey != null) params.put("itemKey", itemKey);
        return new QuestObjectiveConfigDTO(getKey(), getType(), params);
    }

    public int getAmount() {
        return amount;
    }

    public ItemTarget getItem() {
        return item;
    }

    public String getItemKey() {
        return itemKey;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "item_key", itemKey != null ? itemKey : "",
                "material", item.placeholder(),
                "amount", String.valueOf(amount)
        );
    }
    @Override
    public int getTarget() {
        return amount;
    }
}
