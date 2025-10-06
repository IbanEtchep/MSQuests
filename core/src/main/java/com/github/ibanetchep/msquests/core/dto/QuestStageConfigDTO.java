package com.github.ibanetchep.msquests.core.dto;

import com.github.ibanetchep.msquests.core.quest.objective.Flow;

import java.util.List;

public record QuestStageConfigDTO(
        String key,
        String name,
        Flow flow,
        List<QuestObjectiveConfigDTO> objectives
) {
    /**
     * Compact constructor avec validation
     */
    public QuestStageConfigDTO {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Stage key cannot be null or empty");
        }
    }

    public QuestObjectiveConfigDTO getObjective(String key) {
        return objectives == null ? null : objectives.stream()
                .filter(o -> key.equals(o.key()))
                .findFirst()
                .orElse(null);
    }
}
