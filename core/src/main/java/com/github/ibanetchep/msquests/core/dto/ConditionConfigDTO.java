package com.github.ibanetchep.msquests.core.dto;

import java.util.Map;

public record ConditionConfigDTO(String type, Map<String, Object> params) {}
