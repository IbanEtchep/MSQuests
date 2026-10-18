package com.github.ibanetchep.msquests.bukkit.quest.objective;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Artisan's item specs ({@code api.getItems()} or the local runtime): what an objective's
 * {@code item} is built and compared with (vanilla, custom or captured item, name, lore,
 * enchants...).
 */
public interface ItemMatcher {

    /**
     * Whether {@code stack} is the item {@code spec} describes, on the {@code comparisons} named
     * ({@code material}, {@code custom_item}, {@code name}, {@code lore}, {@code enchants},
     * {@code custom_model_data}, {@code damage}, {@code exact}); empty: the criteria the spec carries.
     */
    boolean matches(Map<String, ?> spec, @Nullable ItemStack stack, Set<String> comparisons);

    /** The item the spec describes, null when it cannot be built. */
    @Nullable ItemStack build(Map<String, ?> spec);
}
