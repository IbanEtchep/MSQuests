package com.github.ibanetchep.msquests.core.dto;

import java.time.Instant;
import java.util.List;

public record QuestGroupDTO(
        String key,
        String name,
        String description,
        List<QuestConfigDTO> quests,
        String distributionMode,
        Integer maxActive,
        Integer maxPerPeriod,
        String resetCron,
        Instant startAt,
        Instant endAt
) {}