package com.github.ibanetchep.msquests.bukkit.quest.objective;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The item an objective is about: {@code item}, a spec the suite's runtime builds and compares
 * ({@link ItemMatcher}: vanilla, custom or captured item, name, lore, enchants…), compared on
 * {@code match} (comparison ids; empty: the criteria the item carries), or the legacy bare material under the
 * objective's own key ({@code material}, {@code fish_type}), compared on the material, a custom
 * item built on it refused.
 * Neither: any item.
 */
public final class ItemTarget {

    private static final ItemTarget ANY = new ItemTarget(null, Set.of());

    /** The comparison ids the suite's runtime knows, in its order; anything else in {@code match} is dropped. */
    public static final List<String> COMPARISONS = List.of(
            "material", "custom_item", "name", "lore", "enchants", "custom_model_data", "damage", "exact");

    private final @Nullable Map<String, Object> spec;
    private final Set<String> match;

    private ItemTarget(@Nullable Map<String, Object> spec, Set<String> match) {
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
        return new ItemTarget(Map.of("material", material.name()), comparisons(List.of("material", "custom_item")));
    }

    private static Set<String> readMatch(@Nullable Object raw) {
        if (!(raw instanceof List<?> list)) return Set.of();
        return comparisons(list.stream().map(id -> String.valueOf(id).trim().toLowerCase(Locale.ROOT)).toList());
    }

    /** The known ids among {@code ids}, in {@link #COMPARISONS} order. */
    private static Set<String> comparisons(List<String> ids) {
        Set<String> out = new LinkedHashSet<>();
        for (String id : COMPARISONS) {
            if (ids.contains(id)) out.add(id);
        }
        return Collections.unmodifiableSet(out);
    }

    public boolean isAny() {
        return spec == null;
    }

    /** Whether {@code stack} is this item; always true for any item. */
    public boolean test(@Nullable ItemStack stack, ItemMatcher items) {
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
        if (!match.isEmpty()) out.put("match", List.copyOf(match));
    }

    @Override
    public String toString() {
        return spec == null ? "any" : spec + (match.isEmpty() ? "" : " on " + List.copyOf(match));
    }
}
