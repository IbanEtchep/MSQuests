package com.github.ibanetchep.msquests.bukkit.quest.objective.fishing;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.FISHING)
public class FishingObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "fish_type")
    private @Nullable Material fishType;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public FishingObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if (dto.params().containsKey("fish_type") && dto.params().get("fish_type") != null) {
            fishType = Material.valueOf(dto.params().get("fish_type").toString().toUpperCase());
        }

        if (dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    @Nullable
    public Material getFishType() {
        return fishType;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "fish_type", fishType != null ? "<lang:" + fishType.translationKey() + ">" : "",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "fish_type", fishType != null ? fishType.name() : null,
                        "amount", amount
                )
        );
    }
}
