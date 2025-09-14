package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;

import java.util.Map;

public abstract class QuestObjectiveConfig implements Translatable, PlaceholderProvider {

    protected String key;
    protected String type;
    protected int amount = 1;

    public QuestObjectiveConfig(String key, Map<String, Object> config) {
        this.key = key;
        this.type = (String) config.get("type");

        if(config.containsKey("amount")) {
            this.amount = (int) config.get("amount");
        }
    }

    public abstract Map<String, Object> serialize();

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public int getTargetAmount() {
        return amount;
    }

    @Override
    public String getTranslationKey() {
        return "quest.objective." + type;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "amount", String.valueOf(amount)
        );
    }
}
