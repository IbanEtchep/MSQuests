package com.github.ibanetchep.msquests.core.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestActionDTO {

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private @Nullable String name;

    @JsonProperty("conditions")
    private @Nullable List<ConditionConfigDTO> conditions;

    private final Map<String, Object> params = new HashMap<>();

    public QuestActionDTO() {}

    public QuestActionDTO(String type, @Nullable String name, Map<String, Object> params, @Nullable List<ConditionConfigDTO> conditions) {
        this.type = type;
        this.name = name;
        this.conditions = conditions;
        if (params != null) this.params.putAll(params);
    }

    public String type() { return type; }
    public @Nullable String name() { return name; }
    @JsonAnyGetter
    public Map<String, Object> params() { return params; }
    public @Nullable List<ConditionConfigDTO> conditions() { return conditions; }

    @JsonAnySetter
    public void setParam(String key, Object value) {
        params.put(key, value);
    }
}
