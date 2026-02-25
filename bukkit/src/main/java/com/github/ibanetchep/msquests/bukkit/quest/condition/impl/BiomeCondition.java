package com.github.ibanetchep.msquests.bukkit.quest.condition.impl;

import com.github.ibanetchep.msquests.bukkit.quest.condition.BukkitObjectiveCondition;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public class BiomeCondition implements BukkitObjectiveCondition {

    private final Biome biome;

    public BiomeCondition(Map<String, Object> params) {
        NamespacedKey key = NamespacedKey.minecraft(params.get("biome").toString().toLowerCase());
        this.biome = Objects.requireNonNull(Registry.BIOME.get(key), "Unknown biome: " + key);
    }

    @Override
    public boolean test(Player player) {
        return player.getLocation().getBlock().getBiome() == biome;
    }
}
