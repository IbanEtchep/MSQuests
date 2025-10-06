package com.github.ibanetchep.msquests.core.dto;

import java.util.*;
import java.util.stream.Collectors;

public record QuestConfigDTO(
        String key,
        String name,
        String description,
        Long duration,
        List<QuestActionDTO> rewards,
        List<QuestStageConfigDTO> stages
) {
    /**
     * Compact constructor avec validation
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
     * Valide l'unicité des clés dans une liste
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
     * Helper pour accéder à un stage par clé
     */
    public QuestStageConfigDTO getStage(String key) {
        return stages == null ? null : stages.stream()
                .filter(s -> key.equals(s.key()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper pour obtenir une Map (pour accès rapide)
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
