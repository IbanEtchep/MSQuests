package com.github.ibanetchep.msquests.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record QuestConfigDTO(
        String key,
        String name,
        String description,
        Long duration,
        String tier,
        // An Artisan action chain, keyed steps (see ActionChain).
        List<Object> rewards,
        List<QuestStageConfigDTO> stages,
        // Artisan conditions, keyed (see Conditions).
        @Nullable List<Object> requirements,
        // quest_group Documents only, see QuestGroupConfigDTO#uid.
        @JsonIgnore String uid,
        // quest_group Documents only: actions.start / objective_progress / objective_complete.
        @JsonIgnore @Nullable QuestConfigHooksDTO hooks
) {
    public QuestConfigDTO(String key, String name, String description, Long duration, String tier,
                          List<Object> rewards,
                          List<QuestStageConfigDTO> stages, @Nullable List<Object> requirements) {
        this(key, name, description, duration, tier, rewards, stages, requirements, null, null);
    }

    public QuestConfigDTO(String key, String name, String description, Long duration, String tier,
                          List<Object> rewards, List<QuestStageConfigDTO> stages,
                          @Nullable List<Object> requirements, String uid) {
        this(key, name, description, duration, tier, rewards, stages, requirements, uid, null);
    }

    /**
     * Compact constructor with validation
     */
    public QuestConfigDTO {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Quest key cannot be null or empty");
        }

        if (stages != null) {
            validateUniqueKeys(key, stages, "stage");
        }
    }

    /**
     * Validates key uniqueness in a list
     */
    private static void validateUniqueKeys(String key, List<QuestStageConfigDTO> items, String itemType) {
        Set<String> keys = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (QuestStageConfigDTO item : items) {
            if (item.key() != null && !keys.add(item.key())) {
                duplicates.add(item.key());
            }
        }

        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Duplicate " + itemType + " keys in quest '" + key + "': " +
                            String.join(", ", duplicates)
            );
        }
    }

    /**
     * Helper to access a stage by key
     */
    public QuestStageConfigDTO getStage(String key) {
        return stages == null ? null : stages.stream()
                .filter(s -> key.equals(s.key()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper to get a Map (for fast access)
     */
    public Map<String, QuestStageConfigDTO> stagesAsMap() {
        return stages == null ? Map.of() : stages.stream()
                .collect(Collectors.toMap(
                        QuestStageConfigDTO::key,
                        s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
