package com.github.ibanetchep.artisanattribution.config;

/** The {@code prompt} section of config.yml. */
public record PromptSettings(
        boolean onJoin,
        boolean repeatUntilAnswered,
        boolean newPlayersOnly,
        long delayTicks,
        boolean closeOnAnswer
) {
    public static final PromptSettings DEFAULTS = new PromptSettings(true, true, false, 40, true);
}
