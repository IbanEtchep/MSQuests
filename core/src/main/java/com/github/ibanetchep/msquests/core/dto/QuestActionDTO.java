
package com.github.ibanetchep.msquests.core.dto;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record QuestActionDTO(
        String type,
        @Nullable String name,
        Map<String, Object> params,
        @Nullable List<ConditionConfigDTO> conditions
) {}