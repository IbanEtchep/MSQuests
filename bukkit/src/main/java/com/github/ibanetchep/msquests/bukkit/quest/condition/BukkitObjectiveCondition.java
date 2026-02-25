package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.QuestObjectiveCondition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface BukkitObjectiveCondition extends QuestObjectiveCondition {

    @Override
    default boolean test(PlayerProfile profile) {
        Player player = Bukkit.getPlayer(profile.getId());
        return player != null && test(player);
    }

    boolean test(Player player);
}
