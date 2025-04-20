package com.github.ibanetchep.msquests.core.quest;

public class QuestCategory {

    private final String name;
    private final String description;

    public QuestCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
