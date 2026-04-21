package com.github.ibanetchep.msquests.core.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ConditionConfigDTO {

    @JsonProperty("type")
    private String type;

    private final Map<String, Object> params = new HashMap<>();

    public ConditionConfigDTO() {}

    public static ConditionConfigDTO of(String type, Map<String, Object> params) {
        ConditionConfigDTO dto = new ConditionConfigDTO();
        dto.type = type;
        dto.params.putAll(params);
        return dto;
    }

    public String type() {
        return type;
    }

    @JsonAnyGetter
    public Map<String, Object> params() {
        return params;
    }

    @JsonAnySetter
    public void setParam(String key, Object value) {
        params.put(key, value);
    }
}
