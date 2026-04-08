package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.ConditionConfigDTO;
import com.github.ibanetchep.msquests.core.quest.condition.Condition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConditionFactory {

    private final Map<String, Function<Map<String, Object>, Condition>> creators = new HashMap<>();

    public void register(String type, Function<Map<String, Object>, Condition> creator) {
        creators.put(type, creator);
    }

    public Condition build(ConditionConfigDTO dto) {
        Function<Map<String, Object>, Condition> creator = creators.get(dto.type());
        return creator != null ? creator.apply(dto.params()) : null;
    }
}
