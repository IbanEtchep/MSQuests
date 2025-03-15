package com.github.ibanetchep.msquests.model;

import java.util.Map;
import java.util.UUID;

public class QuestObjective {

    private UUID uniqueId;
    private String type;
    private Map<String, String> config;

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
