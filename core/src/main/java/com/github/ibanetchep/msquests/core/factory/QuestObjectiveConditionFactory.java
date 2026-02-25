package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConditionConfigDTO;
import com.github.ibanetchep.msquests.core.quest.condition.QuestObjectiveCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QuestObjectiveConditionFactory {

    private final Map<String, Function<Map<String, Object>, QuestObjectiveCondition>> creators = new HashMap<>();

    public void register(String type, Function<Map<String, Object>, QuestObjectiveCondition> creator) {
        creators.put(type, creator);
    }

    public QuestObjectiveCondition build(QuestObjectiveConditionConfigDTO dto) {
        Function<Map<String, Object>, QuestObjectiveCondition> creator = creators.get(dto.type());
        return creator != null ? creator.apply(dto.params()) : null;
    }
}
