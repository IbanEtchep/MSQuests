package com.github.ibanetchep.msquests.core.quest.config.condition;

import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.List;
import java.util.Map;

/**
 * Evaluates {@link Conditions} for a player: the requirements of a quest (may it start?) and the
 * conditions of an objective (does this event count?). The platform evaluates them with Artisan's
 * conditions, the same vocabulary as an {@code if:} in a chain.
 */
@FunctionalInterface
public interface ConditionChecker {

    /** Accepts everything: tests, and a platform that evaluates no condition. */
    ConditionChecker ALWAYS = (conditions, profile, args) -> true;

    /**
     * True when every condition holds for the player; an empty list always holds. A player who
     * is not online fails any non-empty list: conditions read the live player.
     *
     * @param args bindings the conditions may read ({@code {quest}}, {@code {objective}}…)
     */
    boolean test(Conditions conditions, PlayerProfile profile, Map<String, Object> args);

    /** One message per condition {@link #test} would reject whatever the player; empty when valid. */
    default List<String> validate(Conditions conditions) {
        return List.of();
    }
}
