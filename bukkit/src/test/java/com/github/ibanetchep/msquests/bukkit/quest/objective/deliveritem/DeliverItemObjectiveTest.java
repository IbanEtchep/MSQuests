package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.bukkit.quest.objective.AbstractObjectiveHandlerTest;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliverItemObjectiveTest extends AbstractObjectiveHandlerTest {

    private DeliverItemObjective objective(int amount, int progress) {
        return objective(Map.of("material", "STONE", "amount", amount), progress);
    }

    private DeliverItemObjective objective(Map<String, Object> params, int progress) {
        DeliverItemObjective.setItems(() -> items);
        return createObjective(stage -> new DeliverItemObjective(stage,
                new DeliverItemObjectiveConfig(new QuestObjectiveConfigDTO("stone", "deliver_item", params)),
                progress, QuestObjectiveStatus.IN_PROGRESS));
    }

    private int count(Material material) {
        return Arrays.stream(player.getInventory().getStorageContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    @Test
    void takesEverythingWhenShortOfTheTarget() {
        player.getInventory().addItem(new ItemStack(Material.STONE, 100));

        assertEquals(100, objective(150, 0).contribute(player).amount());
        assertEquals(0, count(Material.STONE));
    }

    @Test
    void takesOnlyWhatIsStillMissing() {
        player.getInventory().addItem(new ItemStack(Material.STONE, 100));

        assertEquals(30, objective(150, 120).contribute(player).amount());
        assertEquals(70, count(Material.STONE));
    }

    @Test
    void takesOnlyTheItemsMatchingTheSpec() {
        ItemStack named = new ItemStack(Material.STONE, 5);
        named.editMeta(meta -> meta.displayName(net.kyori.adventure.text.Component.text("Pierre bénie")));
        player.getInventory().setItem(0, new ItemStack(Material.STONE, 10));
        player.getInventory().setItem(1, named);

        DeliverItemObjective objective = objective(Map.of(
                "item", Map.of("material", "STONE", "title", "Pierre bénie"),
                "match", java.util.List.of("material", "name"),
                "amount", 20), 0);

        assertEquals(5, objective.contribute(player).amount());
        assertEquals(10, count(Material.STONE));
    }

    @Test
    void aLegacyMaterialPassesTheSchema() {
        String schema = com.github.ibanetchep.msquests.core.util.JsonSchemaGenerator.generateSchema(DeliverItemObjectiveConfig.class);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> com.github.ibanetchep.msquests.core.util.JsonSchemaValidator
                .validate(Map.of("material", "STONE", "amount", 64), schema));
    }

    @AfterEach
    void restoreDefault() {
        DeliverItemObjective.setIncludeShulkerBoxes(true);
    }

    private ItemStack shulkerWith(ItemStack content) {
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        ShulkerBox box = (ShulkerBox) meta.getBlockState();
        box.getInventory().addItem(content);
        meta.setBlockState(box);
        shulker.setItemMeta(meta);
        return shulker;
    }

    private int countInShulker(int slot, Material material) {
        BlockStateMeta meta = (BlockStateMeta) player.getInventory().getItem(slot).getItemMeta();
        return Arrays.stream(((ShulkerBox) meta.getBlockState()).getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    @Test
    void topsUpFromShulkerBoxesCarriedInTheInventory() {
        player.getInventory().setItem(0, new ItemStack(Material.STONE, 10));
        player.getInventory().setItem(1, shulkerWith(new ItemStack(Material.STONE, 20)));

        assertEquals(25, objective(25, 0).contribute(player).amount());
        assertEquals(0, count(Material.STONE));
        assertEquals(5, countInShulker(1, Material.STONE));
    }

    @Test
    void leavesShulkerBoxesAloneWhenDisabled() {
        DeliverItemObjective.setIncludeShulkerBoxes(false);
        player.getInventory().setItem(0, new ItemStack(Material.STONE, 10));
        player.getInventory().setItem(1, shulkerWith(new ItemStack(Material.STONE, 20)));

        assertEquals(10, objective(25, 0).contribute(player).amount());
        assertEquals(20, countInShulker(1, Material.STONE));
    }
}
