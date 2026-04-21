package com.github.ibanetchep.msquests.core.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class QuestObjectiveConfigDTO {

    @JsonProperty("key")
    private String key;

    @JsonProperty("type")
    private String type;

    private final Map<String, Object> params = new HashMap<>();

    public QuestObjectiveConfigDTO() {}

    public QuestObjectiveConfigDTO(String key, String type, Map<String, Object> params) {
        this.key = key;
        this.type = type;
        if (params != null) this.params.putAll(params);
    }

    public String key() { return key; }
    public String type() { return type; }
    @JsonAnyGetter
    public Map<String, Object> params() { return params; }

    @JsonAnySetter
    public void setParam(String key, Object value) {
        params.put(key, value);
    }
}
