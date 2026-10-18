package com.github.ibanetchep.msquests.bukkit.suite;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/** Artisan's {@code ConditionsApi} or the local engine: a condition list read strictly and evaluated for a player. */
public interface ConditionsPort {

    /** On the thread that owns the player; elsewhere an {@link IllegalStateException}. */
    boolean test(Player player, List<?> conditions, Map<String, ?> args);

    /** One problem per unknown or malformed condition, empty when all are readable. */
    List<String> validate(List<?> conditions);
}
