package com.github.ibanetchep.artisanattribution.config;

import com.github.ibanetchep.artisanattribution.storage.PlayerAttribution;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptPolicyTest {

    private static final UUID ID = UUID.randomUUID();
    private static final PlayerAttribution NEVER_ASKED = PlayerAttribution.unknown(ID);
    private static final PlayerAttribution ASKED_ONCE = NEVER_ASKED.withPrompt(1_000);
    private static final PlayerAttribution ANSWERED = ASKED_ONCE.withAnswer("discord", 2_000);

    private static PromptSettings settings(boolean onJoin, boolean repeat, boolean newOnly) {
        return new PromptSettings(onJoin, repeat, newOnly, 40, true);
    }

    @Test
    void asksUntilAnsweredWhenRepeating() {
        PromptSettings s = settings(true, true, false);
        assertTrue(PromptPolicy.shouldPromptOnJoin(s, NEVER_ASKED, true, false));
        assertTrue(PromptPolicy.shouldPromptOnJoin(s, ASKED_ONCE, true, false));
        assertFalse(PromptPolicy.shouldPromptOnJoin(s, ANSWERED, true, false));
    }

    @Test
    void asksOnceWhenNotRepeating() {
        PromptSettings s = settings(true, false, false);
        assertTrue(PromptPolicy.shouldPromptOnJoin(s, NEVER_ASKED, true, false));
        assertFalse(PromptPolicy.shouldPromptOnJoin(s, ASKED_ONCE, true, false));
    }

    @Test
    void neverAsksWhenDisabledOrBypassed() {
        assertFalse(PromptPolicy.shouldPromptOnJoin(settings(false, true, false), NEVER_ASKED, false, false));
        assertFalse(PromptPolicy.shouldPromptOnJoin(settings(true, true, false), NEVER_ASKED, false, true));
    }

    @Test
    void newPlayersOnlyLeavesExistingPlayersAlone() {
        PromptSettings s = settings(true, true, true);
        assertTrue(PromptPolicy.shouldPromptOnJoin(s, NEVER_ASKED, false, false));
        assertFalse(PromptPolicy.shouldPromptOnJoin(s, NEVER_ASKED, true, false));
        // Asked at their first join: still asked at the next ones, though they have played before.
        assertTrue(PromptPolicy.shouldPromptOnJoin(s, ASKED_ONCE, true, false));
    }

    @Test
    void aResetPlayerIsAskedAgain() {
        PlayerAttribution reset = ANSWERED.cleared();
        assertTrue(PromptPolicy.shouldPromptOnJoin(settings(true, false, true), reset, true, false));
    }
}
