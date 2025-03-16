package com.github.ibanetchep.msquests.model.quest;

import java.util.Map;

public abstract class QuestObjectiveDefinition {

    protected String name;
    protected String description;

    public QuestObjectiveDefinition(Map<String, String> config) {
        this.name = config.get("name");
        this.description = config.get("description");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
