package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupType;

import java.time.Instant;
import java.util.List;

public record QuestGroupDTO(
        String key,
        String name,
        String description,
        List<QuestConfigDTO> quests,
        QuestGroupType type,
        Integer maxActiveQuests,
        Integer maxPerPeriod,
        String periodSwitchCron,
        Instant startAt,
        Instant endAt
) {}