package com.github.ibanetchep.msquests.core.dto;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public record QuestGroupConfigDTO(
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
) {
    public QuestGroupConfigDTO {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Group key cannot be null or empty");
        }

        if (quests != null) {
            validateUniqueKeys(quests, "quest");
        }
    }

    private static void validateUniqueKeys(List<QuestConfigDTO> items, String itemType) {
        Set<String> keys = new HashSet<>();
        List<String> duplicates = new ArrayList<>();

        for (QuestConfigDTO item : items) {
            if (item.key() != null && !keys.add(item.key())) {
                duplicates.add(item.key());
            }
        }

        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Duplicate " + itemType + " keys: " + String.join(", ", duplicates)
            );
        }
    }

    public QuestConfigDTO getQuest(String key) {
        return quests == null ? null : quests.stream()
                .filter(q -> key.equals(q.key()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, QuestConfigDTO> questsAsMap() {
        return quests == null ? Map.of() : quests.stream()
                .collect(Collectors.toMap(
                        QuestConfigDTO::key,
                        s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
