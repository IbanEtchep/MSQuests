package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.dto.RewardDTO;
import com.github.ibanetchep.msquests.core.quest.reward.Reward;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RewardTypeRegistry {

    private final Map<String, Function<RewardDTO, Reward>> registeredTypes = new HashMap<>();

    /**
     * Register a reward type with its factory function.
     *
     * @param type key of the reward type, e.g. "item", "command"
     * @param factory function that converts RewardDTO to Reward
     */
    public void registerType(String type, Function<RewardDTO, Reward> factory) {
        registeredTypes.put(type, factory);
    }

    /**
     * Creates a Reward from its config DTO.
     */
    public Reward createReward(RewardDTO config) {
        Function<RewardDTO, Reward> factory = registeredTypes.get(config.type());
        if (factory == null) throw new IllegalArgumentException("Unknown reward type: " + config.type());
        return factory.apply(config);
    }

    public Map<String, Function<RewardDTO, Reward>> getRegisteredTypes() {
        return Collections.unmodifiableMap(registeredTypes);
    }
}
