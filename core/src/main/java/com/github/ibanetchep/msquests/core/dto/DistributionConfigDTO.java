package com.github.ibanetchep.msquests.core.dto;

import java.util.List;

public record DistributionConfigDTO(
        String strategy,
        Integer amount,
        List<String> triggers
) {
}
