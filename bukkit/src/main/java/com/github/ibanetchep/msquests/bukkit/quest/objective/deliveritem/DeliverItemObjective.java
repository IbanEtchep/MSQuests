package com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem;

import com.github.ibanetchep.msquests.bukkit.quest.objective.ContributableObjective;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemTarget;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ItemMatcher;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DeliverItemObjective extends AbstractQuestObjective<DeliverItemObjectiveConfig> implements ContributableObjective {

    // ponytail: static because MSQuests builds objectives reflectively; set by the plugin on (re)load.
    private static boolean includeShulkerBoxes = true;
    private static Supplier<ItemMatcher> items = () -> {
        throw new IllegalStateException("The item matcher is not available yet");
    };

    public DeliverItemObjective(QuestStage questStage, DeliverItemObjectiveConfig objectiveConfig, int progress, QuestObjectiveStatus status) {
        super(questStage, objectiveConfig, progress, status);
    }

    public static void setIncludeShulkerBoxes(boolean include) {
        includeShulkerBoxes = include;
    }

    public static void setItems(Supplier<ItemMatcher> api) {
        items = api;
    }

    /**
     * Takes the items matching the objective's {@code item}, from the inventory first, then from
     * the shulker boxes carried in it (unless disabled).
     */
    @Override
    public Contribution contribute(Player player) {
        ItemTarget target = getObjectiveConfig().getItem();
        Map<String, String> placeholders = Map.of("material", target.placeholder());
        // No item: delivering "anything" would empty the inventory.
        if (target.isAny()) {
            return new Contribution(0, "objective.contribute.nothing_to_deliver", placeholders);
        }
        ItemMatcher api = items.get();
        Predicate<ItemStack> wanted = stack -> target.test(stack, api);
        PlayerInventory inventory = player.getInventory();

        int missing = getTarget() - getProgress();
        int taken = take(inventory, wanted, missing);

        if (includeShulkerBoxes) {
            ItemStack[] storage = inventory.getStorageContents();
            for (int slot = 0; slot < storage.length && taken < missing; slot++) {
                ItemStack item = storage[slot];
                if (item == null || !(item.getItemMeta() instanceof BlockStateMeta meta)
                        || !(meta.getBlockState() instanceof ShulkerBox box)) continue;

                int fromBox = take(box.getInventory(), wanted, missing - taken);
                if (fromBox == 0) continue;
                meta.setBlockState(box);
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
                taken += fromBox;
            }
        }

        if (taken == 0) {
            return new Contribution(0, "objective.contribute.nothing_to_deliver", placeholders);
        }
        return new Contribution(taken, "objective.contribute.delivered",
                Map.of("material", placeholders.get("material"), "amount", String.valueOf(taken)));
    }

    /** Removes up to {@code max} items matching {@code wanted}, slot by slot, and returns how many went. */
    private static int take(Inventory inventory, Predicate<ItemStack> wanted, int max) {
        ItemStack[] contents = inventory.getStorageContents();
        int taken = 0;
        for (int slot = 0; slot < contents.length && taken < max; slot++) {
            ItemStack stack = contents[slot];
            if (stack == null || stack.getType().isAir() || !wanted.test(stack)) continue;
            int amount = Math.min(stack.getAmount(), max - taken);
            stack.setAmount(stack.getAmount() - amount);
            contents[slot] = stack.getAmount() > 0 ? stack : null;
            taken += amount;
        }
        if (taken > 0) inventory.setStorageContents(contents);
        return taken;
    }
}
