package com.github.ibanetchep.msquests.core.quest;

import java.util.Map;
import java.util.UUID;

public abstract class QuestObjectiveDefinition {

    protected UUID id;
    protected String name;
    protected String description;
    protected int targetAmount;

    public QuestObjectiveDefinition(Map<String, String> config) {
        this.name = config.get("name");
        this.description = config.get("description");
    }

    public UUID getId() {
        return id;
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
