package com.github.ibanetchep.artisanattribution.config;

import org.jetbrains.annotations.Nullable;

/**
 * One answer offered to the players.
 *
 * @param id what is stored for the player who picks it
 * @param actions the Artisan action chain run when a player picks it, as read from YAML
 *     (a list of keyed steps, a single step, or a legacy DSL string); null when none
 */
public record AttributionSource(
        String id,
        LocalizedText name,
        LocalizedText description,
        String material,
        @Nullable Object actions
) {
}
