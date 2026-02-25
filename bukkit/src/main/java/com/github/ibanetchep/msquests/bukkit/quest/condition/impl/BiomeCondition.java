package com.github.ibanetchep.msquests.bukkit.quest.condition.impl;

import com.github.ibanetchep.msquests.bukkit.quest.condition.BukkitObjectiveCondition;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public class BiomeCondition implements BukkitObjectiveCondition {

    private final Biome biome;

    public BiomeCondition(Map<String, Object> params) {
        NamespacedKey key = NamespacedKey.minecraft(params.get("biome").toString().toLowerCase());
        this.biome = Objects.requireNonNull(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key),
                "Unknown biome: " + key
        );
    }

    @Override
    public boolean test(Player player) {
        return player.getWorld().getBiome(player.getLocation()) == biome;
    }
}
