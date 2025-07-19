package com.github.ibanetchep.msquests.core.dto;

import java.util.List;
import java.util.Map;

public record QuestGroupDTO(
        String key,
        String name,
        Map<String, QuestConfigDTO> quests
) {}