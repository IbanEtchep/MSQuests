package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import org.bukkit.entity.EntityType;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.KILL_ENTITY)
public class KillEntityObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "entity_type")
    private EntityType entityType;

    @ConfigField(name = "amount", required = true)
    private int amount;

    public KillEntityObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
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
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "entity_type", "<lang:" + entityType.translationKey() + ">",
                "amount", String.valueOf(amount)
        );
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
