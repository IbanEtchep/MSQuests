package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Quest requirements and objective conditions, evaluated by the suite's engine
 * ({@link ConditionsPort}: Artisan's {@code api.getConditions()} or the local runtime): the
 * vocabulary of an {@code if:} in a chain, read strictly (an unknown or malformed condition
 * fails). The bindings only reach {@code expression} conditions.
 *
 * <p>The engine reads the player's state on the thread that owns the player; called from
 * elsewhere (a menu rendered for another player on Folia) the check fails closed, with one warning.
 */
public final class PlayerConditionChecker implements ConditionChecker {

    private final ConditionsPort conditions;
    private final Logger logger;
    private final AtomicBoolean wrongThreadWarned = new AtomicBoolean();

    public PlayerConditionChecker(ConditionsPort conditions, Logger logger) {
        this.conditions = conditions;
        this.logger = logger;
    }

    @Override
    public boolean test(Conditions list, PlayerProfile profile, Map<String, Object> args) {
        if (list.isEmpty()) return true;
        Player player = Bukkit.getPlayer(profile.getId());
        if (player == null) return false;
        try {
            return conditions.test(player, list.conditions(), args);
        } catch (IllegalStateException e) {
            if (wrongThreadWarned.compareAndSet(false, true)) {
                logger.warning("Quest conditions checked off the player's thread, treated as not met: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    public List<String> validate(Conditions list) {
        return list.isEmpty() ? List.of() : conditions.validate(list.conditions());
    }
}
