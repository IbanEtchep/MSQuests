package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ActionTypeRegistry {

    private final Map<String, Function<QuestActionDTO, QuestAction>> registeredTypes = new HashMap<>();

    /**
     * Register a reward type with its factory function.
     *
     * @param type key of the reward type, e.g. "item", "command"
     * @param factory function that converts RewardDTO to Reward
     */
    public void registerType(String type, Function<QuestActionDTO, QuestAction> factory) {
        registeredTypes.put(type, factory);
    }

    /**
     * Creates a Reward from its config DTO.
     */
    public QuestAction createAction(QuestActionDTO config) {
        Function<QuestActionDTO, QuestAction> factory = registeredTypes.get(config.type());
        if (factory == null) throw new IllegalArgumentException("Unknown action type: " + config.type());
        return factory.apply(config);
    }

    public Map<String, Function<QuestActionDTO, QuestAction>> getRegisteredTypes() {
        return Collections.unmodifiableMap(registeredTypes);
    }
}
