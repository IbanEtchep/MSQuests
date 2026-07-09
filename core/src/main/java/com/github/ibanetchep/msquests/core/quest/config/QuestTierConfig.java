package com.github.ibanetchep.msquests.core.quest.config;

public class QuestTierConfig {

    private final String key;
    private final String name;

    public QuestTierConfig(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
