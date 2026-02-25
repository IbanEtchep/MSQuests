package com.github.ibanetchep.msquests.bukkit.quest.condition.impl;

import com.github.ibanetchep.msquests.bukkit.quest.condition.BukkitObjectiveCondition;
import org.bukkit.entity.Player;

import java.util.Map;

public class WorldCondition implements BukkitObjectiveCondition {

    private final String worldName;

    public WorldCondition(Map<String, Object> params) {
        this.worldName = (String) params.get("world");
    }

    @Override
    public boolean test(Player player) {
        return player.getWorld().getName().equals(worldName);
    }
}
