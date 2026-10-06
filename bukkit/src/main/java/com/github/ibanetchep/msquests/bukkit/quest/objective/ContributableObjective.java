package com.github.ibanetchep.msquests.bukkit.quest.objective;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * An objective a player advances on demand, from what they own — items in their inventory,
 * money in a bank — rather than from a game event. Driven by the {@code msquests:contribute}
 * menu command.
 */
public interface ContributableObjective extends QuestObjective {

    /**
     * Takes what the player can give, never more than what is still missing. Runs on the main
     * thread. The caller turns the returned amount into progress and sends the message.
     */
    Contribution contribute(Player player);

    /** {@code amount} taken (0 = nothing), and the translation key explaining it to the player. */
    record Contribution(int amount, String messageKey, Map<String, String> placeholders) {}
}
