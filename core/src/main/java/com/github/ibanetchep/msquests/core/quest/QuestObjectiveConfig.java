package com.github.ibanetchep.msquests.core.quest;

import java.util.Map;

public abstract class QuestObjectiveConfig {

    protected String key;
    protected String type;
    protected int amount;

    public QuestObjectiveConfig(String key, Map<String, Object> config) {
        this.key = key;
        this.type = (String) config.get("type");

        if(config.containsKey("amount")) {
            this.amount = (int) config.get("amount");
        } else {
            this.amount = 1;
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
}
