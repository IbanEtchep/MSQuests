package com.github.ibanetchep.msquests.bukkit.config;

import java.util.Locale;

/**
 * {@code content_source} in {@code config.yml}: where quests and their progress are read from.
 * Read once at startup; switching needs a restart.
 */
public enum ContentSource {
    /** {@code quests/*.yml} and the {@code msquests_*} tables, as always. */
    LEGACY,
    /** {@code <project>/quest_groups/*.yaml} of every Artisan project ({@code quest_group} Documents) and the {@code artisan_quests_*} tables. */
    ARTISAN;

    public static ContentSource parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("content_source must be legacy or artisan, got '" + value + "'");
        }
    }
}
