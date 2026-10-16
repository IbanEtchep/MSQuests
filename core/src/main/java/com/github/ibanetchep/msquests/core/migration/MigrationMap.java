package com.github.ibanetchep.msquests.core.migration;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the conversion of {@code quests/*.yml} to {@code quest_group} Documents decided, written
 * to {@code migration/map.yaml}: the uid given to each group and quest, and the stage each
 * objective key belongs to. The copy of {@code msquests_*} into {@code artisan_quests_*} reads it
 * to turn the stored {@code (group_key, quest_key)} and {@code objective_key} into
 * {@code quest_uid} and {@code stage_key}; a later conversion reads it to keep the same uids.
 */
public record MigrationMap(Map<String, Group> groups) {

    public MigrationMap {
        groups = Map.copyOf(groups);
    }

    public static MigrationMap empty() {
        return new MigrationMap(Map.of());
    }

    /** @param quests by quest key, in group order */
    public record Group(String uid, Map<String, Quest> quests) {
        public Group {
            quests = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(quests));
        }
    }

    /** @param objectiveStages objective key → key of the stage holding it */
    public record Quest(String uid, Map<String, String> objectiveStages) {
        public Quest {
            objectiveStages = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(objectiveStages));
        }
    }

    public @Nullable Group group(String groupKey) {
        return groups.get(groupKey);
    }

    public @Nullable Quest quest(String groupKey, String questKey) {
        Group group = groups.get(groupKey);
        return group != null ? group.quests().get(questKey) : null;
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }
}
