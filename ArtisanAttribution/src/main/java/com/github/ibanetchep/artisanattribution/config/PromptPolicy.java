package com.github.ibanetchep.artisanattribution.config;

import com.github.ibanetchep.artisanattribution.storage.PlayerAttribution;

/** Whether a joining player gets the menu. */
public final class PromptPolicy {

    private PromptPolicy() {
    }

    /**
     * @param playedBefore whether the player had joined the server before this join
     * @param bypass whether the player holds {@code artisanattribution.bypass}
     */
    public static boolean shouldPromptOnJoin(PromptSettings settings, PlayerAttribution player,
                                             boolean playedBefore, boolean bypass) {
        if (!settings.onJoin() || bypass || player.answered()) return false;
        if (player.everPrompted()) return settings.repeatUntilAnswered();
        // Never asked yet. With new_players_only, the players already there at install are left
        // alone; a player asked at their first join, or reset by an admin, has a row and stays eligible.
        return !settings.newPlayersOnly() || !playedBefore || player.stored();
    }
}
