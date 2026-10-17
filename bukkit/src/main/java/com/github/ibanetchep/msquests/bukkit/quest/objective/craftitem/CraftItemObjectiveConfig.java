package com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemTarget;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;

import java.util.LinkedHashMap;
import java.util.Map;

@ObjectiveType(ObjectiveTypes.CRAFT_ITEM)
public class CraftItemObjectiveConfig extends QuestObjectiveConfig {

    /** {@code item} + {@code match}, or the legacy {@code material}. */
    @ConfigField(name = "item")
    private final ItemTarget item;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public CraftItemObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        item = ItemTarget.read(dto.params(), "material");
        if (dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    public ItemTarget getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "material", item.placeholder(),
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        Map<String, Object> params = new LinkedHashMap<>();
        item.write(params);
        params.put("amount", amount);
        return new QuestObjectiveConfigDTO(getKey(), getType(), params);
    }

    @Override
    public int getTarget() {
        return amount;
    }
}
