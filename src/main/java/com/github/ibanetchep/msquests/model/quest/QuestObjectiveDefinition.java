package com.github.ibanetchep.msquests.model.quest;

import java.util.Map;
import java.util.UUID;

public abstract class QuestObjectiveDefinition {

    protected UUID id;
    protected String name;
    protected String description;

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
}
