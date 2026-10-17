package com.github.ibanetchep.msquests.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public record QuestGroupConfigDTO(
        String key,
        String name,
        String description,
        List<QuestConfigDTO> quests,
        Integer maxActive,
        Integer maxPerPeriod,
        String resetCron,
        Instant startAt,
        Instant endAt,
        String actorType,
        QuestGroupConfigActionsDTO actions,
        Map<String, QuestTierConfigDTO> tiers,
        Map<String, Integer> tierDistribution,
        Boolean rotatable,
        Integer maxRotationsPerPeriod,
        DistributionConfigDTO distribution,
        // Renders one objective of the {objectives} chain binding; null: the default line.
        String objectiveLine,
        // Only quest_group Documents carry these (null otherwise); quests/*.yml never does, so a
        // legacy file cannot smuggle in an id the database would then key rows by.
        @JsonIgnore String uid,
        @JsonIgnore String resetTimezone
) {
    public QuestGroupConfigDTO(String key, String name, String description, List<QuestConfigDTO> quests,
                               Integer maxActive, Integer maxPerPeriod, String resetCron, Instant startAt,
                               Instant endAt, String actorType, QuestGroupConfigActionsDTO actions,
                               Map<String, QuestTierConfigDTO> tiers, Map<String, Integer> tierDistribution,
                               Boolean rotatable, Integer maxRotationsPerPeriod, DistributionConfigDTO distribution) {
        this(key, name, description, quests, maxActive, maxPerPeriod, resetCron, startAt, endAt, actorType,
                actions, tiers, tierDistribution, rotatable, maxRotationsPerPeriod, distribution, null, null, null);
    }

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
