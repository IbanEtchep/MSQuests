package com.github.ibanetchep.msquests.core.dto;

import java.util.List;

/**
 * A quest's own hooks besides {@code complete} (carried by {@link QuestConfigDTO#rewards()}),
 * each an Artisan action chain in keyed form (see ActionChain). They run after the group's
 * hook of the same event.
 */
public record QuestConfigHooksDTO(
        List<Object> start,
        List<Object> objectiveProgress,
        List<Object> objectiveComplete
) {
}
