package com.github.ibanetchep.msquests.bukkit.quest.objective.travel;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;

import java.util.Map;

@ObjectiveType(ObjectiveTypes.TRAVEL)
public class TravelObjectiveConfig extends QuestObjectiveConfig {

    @ConfigField(name = "distance", required = true)
    private int distance = 100;

    public TravelObjectiveConfig(QuestObjectiveConfigDTO dto) {
        super(dto);
        if (dto.params().containsKey("distance")) {
            distance = (int) dto.params().get("distance");
        }
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of("distance", String.valueOf(distance));
    }

    @Override
    public QuestObjectiveConfigDTO toDTO() {
        return new QuestObjectiveConfigDTO(
                getKey(),
                getType(),
                Map.of("distance", distance)
        );
    }
}
