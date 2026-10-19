package com.github.ibanetchep.artisanattribution.config;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * config.yml, read once and swapped whole on reload: every reader sees one consistent version.
 *
 * @param sources by id, in the order of config.yml
 * @param onAnswer the action chain run for every answer, after the source's own; null when none
 */
public record AttributionConfig(
        String menu,
        PromptSettings prompt,
        @Nullable Object onAnswer,
        Map<String, AttributionSource> sources
) {
    public @Nullable AttributionSource source(String id) {
        return sources.get(id);
    }

    public List<AttributionSource> orderedSources() {
        return List.copyOf(sources.values());
    }
}
