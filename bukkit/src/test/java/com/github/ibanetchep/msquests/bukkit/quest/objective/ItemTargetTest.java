package com.github.ibanetchep.msquests.bukkit.quest.objective;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemTargetTest {

    private static Map<String, Object> written(ItemTarget target) {
        Map<String, Object> out = new LinkedHashMap<>();
        target.write(out);
        return out;
    }

    @Test
    void readsAnItemSpecAndItsCriteria() {
        ItemTarget target = ItemTarget.read(Map.of(
                "item", Map.of("material", "item:rod_lure3"),
                "match", List.of("material", "Enchants", "repair")), "material");

        assertFalse(target.isAny());
        assertEquals(Map.of("item", Map.of("material", "item:rod_lure3"), "match", List.of("material", "enchants")),
                written(target), "unknown criteria are dropped");
        assertEquals("item:rod_lure3", target.placeholder());
    }

    @Test
    void aSpecWithoutCriteriaKeepsTheDefault() {
        ItemTarget target = ItemTarget.read(Map.of("item", Map.of("material", "nexo:ruby", "title", "Ruby")), "material");

        assertEquals(Map.of("item", Map.of("material", "nexo:ruby", "title", "Ruby")), written(target));
        assertEquals("Ruby", target.placeholder());
    }

    @Test
    void readsTheLegacyMaterialAsAVanillaMaterialComparison() {
        ItemTarget target = ItemTarget.read(Map.of("fish_type", "cod"), "fish_type");

        assertEquals(Map.of("item", Map.of("material", "COD"), "match", List.of("material", "custom_item")), written(target),
                "a custom item built on the material does not count");
        assertEquals("<lang:item.minecraft.cod>", target.placeholder());
        assertThrows(IllegalArgumentException.class, () -> ItemTarget.read(Map.of("material", "NOPE"), "material"));
    }

    @Test
    void nothingMeansAnyItem() {
        ItemTarget target = ItemTarget.read(Map.of("amount", 3), "material");

        assertTrue(target.isAny());
        assertTrue(target.test(null, null));
        assertEquals(Map.of(), written(target));
    }
}
