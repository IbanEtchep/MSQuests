package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * Outcome of reading every quest group file: the groups that parsed, and one message per file
 * that did not. A reload uses the failures to refuse the whole new state instead of silently
 * dropping the broken groups.
 */
public record QuestConfigLoad(Map<String, QuestGroupConfigDTO> groups, List<String> failures) {

    public QuestConfigLoad {
        groups = Map.copyOf(groups);
        failures = List.copyOf(failures);
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }
}
