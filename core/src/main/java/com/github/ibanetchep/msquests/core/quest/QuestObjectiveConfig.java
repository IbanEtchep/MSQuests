package com.github.ibanetchep.msquests.core.quest;

import java.util.Map;

public abstract class QuestObjectiveConfig {

    protected String key;
    protected String name;
    protected String description;
    protected int targetAmount;

    public QuestObjectiveConfig(String key, Map<String, String> config) {
        this.key = key;
        this.name = config.get("name");
        this.description = config.get("description");
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return the target amount of the objective
     */
    public int getTargetAmount() {
        return targetAmount;
    }

    /**
     * Sets the target amount of the objective
     *
     * @param targetAmount the target amount to set
     */
    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }
}
