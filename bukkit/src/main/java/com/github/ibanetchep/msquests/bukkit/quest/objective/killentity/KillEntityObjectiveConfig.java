package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.entity.EntityType;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.KILL_ENTITY)
public class KillEntityObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "entity_type", required = true)
    private  EntityType entityType;

    @ConfigField(name = "amount", required = true)
    private int amount = 1;

    public KillEntityObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);

        if(dto.params().containsKey("entity_type")) {
            entityType = EntityType.valueOf(dto.params().get("entity_type").toString().toUpperCase());
        }

        if(dto.params().containsKey("amount")) {
            amount = (int) dto.params().get("amount");
        }
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of(
                        "amount", amount,
                        "entity_type", entityType.name()
                )
        );
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "entity_type", "<lang:" + entityType.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
