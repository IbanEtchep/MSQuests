package com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.BREED_ANIMAL)
public class BreedAnimalObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "entity_type")
    private @Nullable EntityType entityType;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public BreedAnimalObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if (dto.params().containsKey("entity_type") && dto.params().get("entity_type") != null) {
            entityType = EntityType.valueOf(dto.params().get("entity_type").toString().toUpperCase());
        }
        if (dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    public @Nullable EntityType getEntityType() {
        return entityType;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "entity_type", entityType != null ? "<lang:" + entityType.translationKey() + ">" : "",
                "amount", String.valueOf(amount)
        );
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "entity_type", entityType != null ? entityType.name() : null,
                        "amount", amount
                )
        );
    }
}
