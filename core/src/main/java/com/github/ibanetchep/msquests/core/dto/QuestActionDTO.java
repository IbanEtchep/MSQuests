
package com.github.ibanetchep.msquests.core.dto;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record QuestActionDTO(
        String type,
        @Nullable String name,
        Map<String, Object> params
) {}