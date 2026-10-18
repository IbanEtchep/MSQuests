package com.github.ibanetchep.msquests.bukkit.quest.objective;

import net.artisanmc.modules.api.item.ItemComparison;
import net.artisanmc.modules.api.item.ItemStackApi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The item an objective is about: {@code item}, a spec Artisan builds and compares
 * ({@code api.getItems()}: vanilla, custom or captured item, name, lore, enchants…), compared on
 * {@code match} (empty: the criteria the item carries), or the legacy bare material under the
 * objective's own key ({@code material}, {@code fish_type}), compared on the material, a custom
 * item built on it refused.
 * Neither: any item.
 */
public final class ItemTarget {

    private static final ItemTarget ANY = new ItemTarget(null, Set.of());

    private final @Nullable Map<String, Object> spec;
    private final Set<ItemComparison> match;

    private ItemTarget(@Nullable Map<String, Object> spec, Set<ItemComparison> match) {
        this.spec = spec;
        this.match = match;
    }

    /** Reads {@code item} and {@code match}, else the legacy material at {@code legacyKey}. */
    public static ItemTarget read(Map<String, Object> params, String legacyKey) {
        if (params.get("item") instanceof Map<?, ?> raw && !raw.isEmpty()) {
            Map<String, Object> spec = new LinkedHashMap<>();
            raw.forEach((k, v) -> spec.put(String.valueOf(k), v));
            return new ItemTarget(Collections.unmodifiableMap(spec), readMatch(params.get("match")));
        }
        Object legacy = params.get(legacyKey);
        if (legacy == null || legacy.toString().isBlank()) return ANY;
        Material material = Material.matchMaterial(legacy.toString().trim());
        if (material == null) throw new IllegalArgumentException("Unknown material '" + legacy + "' in " + legacyKey);
        return new ItemTarget(Map.of("material", material.name()), EnumSet.of(ItemComparison.MATERIAL, ItemComparison.CUSTOM_ITEM));
    }

    private static Set<ItemComparison> readMatch(@Nullable Object raw) {
        if (!(raw instanceof List<?> list)) return Set.of();
        EnumSet<ItemComparison> out = EnumSet.noneOf(ItemComparison.class);
        for (Object id : list) {
            ItemComparison c = ItemComparison.fromId(String.valueOf(id));
            if (c != null) out.add(c);
        }
        return out;
    }

    public boolean isAny() {
        return spec == null;
    }

    /** Whether {@code stack} is this item; always true for any item. */
    public boolean test(@Nullable ItemStack stack, ItemStackApi items) {
        if (spec == null) return true;
        if (stack == null) return false;
        return items.matches(spec, stack, match);
    }

    /** The vanilla material when the item is one, for its translated name. */
    public @Nullable Material vanillaMaterial() {
        return spec == null ? null : Material.matchMaterial(String.valueOf(spec.get("material")));
    }

    /** The item's name for a message: its title, else its translated material, else its ref. */
    public String placeholder() {
        if (spec == null) return "";
        if (spec.get("title") instanceof String title && !title.isBlank()) return title;
        Material material = vanillaMaterial();
        if (material != null) return "<lang:" + material.translationKey() + ">";
        Object ref = spec.get("material");
        return ref == null ? "" : ref.toString();
    }

    /** Writes it back as read: {@code item} and {@code match}, nothing for any item. */
    public void write(Map<String, Object> out) {
        if (spec == null) return;
        out.put("item", spec);
        if (!match.isEmpty()) out.put("match", match.stream().map(ItemComparison::id).toList());
    }

    @Override
    public String toString() {
        return spec == null ? "any" : spec + (match.isEmpty() ? "" : " on " + match.stream().map(ItemComparison::id).toList());
    }
}
