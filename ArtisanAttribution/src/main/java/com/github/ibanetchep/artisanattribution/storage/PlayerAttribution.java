package com.github.ibanetchep.artisanattribution.storage;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * What is known of one player. Times are epoch millis.
 *
 * @param source the source id they picked, null until they answer
 * @param promptedAt the last time the menu was opened for them on join, null if never
 * @param prompts how many times the menu was opened for them on join
 * @param stored whether the database holds a row for them: asked, answered, or reset by an admin
 */
public record PlayerAttribution(
        UUID playerId,
        @Nullable String source,
        @Nullable Long answeredAt,
        @Nullable Long promptedAt,
        int prompts,
        boolean stored
) {
    public static PlayerAttribution unknown(UUID playerId) {
        return new PlayerAttribution(playerId, null, null, null, 0, false);
    }

    public boolean answered() {
        return source != null;
    }

    /** As after {@link AttributionRepository#reset}: the answer and the prompts forgotten, the row kept. */
    public PlayerAttribution cleared() {
        return new PlayerAttribution(playerId, null, null, null, 0, true);
    }

    public boolean everPrompted() {
        return promptedAt != null;
    }

    public PlayerAttribution withAnswer(String source, long at) {
        return new PlayerAttribution(playerId, source, at, promptedAt, prompts, true);
    }

    public PlayerAttribution withPrompt(long at) {
        return new PlayerAttribution(playerId, source, answeredAt, at, prompts + 1, true);
    }
}
