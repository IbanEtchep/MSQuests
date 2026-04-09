package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.Material;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.HARVEST_CROP)
public class HarvestCropObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "crop", required = true)
    private Material crop;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public HarvestCropObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if (dto.params().containsKey("crop")) {
            crop = Material.valueOf(dto.params().get("crop").toString().toUpperCase());
        }
        if (dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    public Material getCrop() {
        return crop;
    }

    public int getAmount() {
        return amount;
    }

    public boolean requiresAgeCheck() {
        return crop != Material.MELON && crop != Material.PUMPKIN;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "crop", "<lang:" + crop.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "crop", crop.name(),
                        "amount", amount
                )
        );
    }
}
