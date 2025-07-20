package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import org.bukkit.Material;

import java.util.Map;

public class DeliverItemObjectiveConfig extends QuestObjectiveConfig {

    // Key of saved quest item (optional)
    private String itemKey;
    private Material material;

    public DeliverItemObjectiveConfig(String key, Map<String, Object> config) {
        super(key, config);

        if(config.containsKey("itemKey")) {
            this.itemKey = (String) config.get("itemKey");
        }

        if (config.containsKey("material")) {
            this.material = Material.valueOf((String) config.get("material"));
        }
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "itemKey", itemKey,
                "material", material.name(),
                "amount", amount
        );
    }
}
