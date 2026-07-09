package com.github.ibanetchep.msquests.bukkit.quest.objective.craftitem;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftItemObjectiveHandler extends BukkitQuestObjectiveHandler<CraftItemObjective> implements Listener {

    public CraftItemObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.CRAFT_ITEM;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getRecipe().getResult();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());
        Material craftedMaterial = result.getType();
        int amount = result.getAmount();

        for (CraftItemObjective objective : getEligibleObjectives(profile)) {
            Material material = objective.getObjectiveConfig().getMaterial();
            if (material == null || material == craftedMaterial) {
                plugin.getQuestProgressService().progressObjective(objective, amount, profile);
            }
        }
    }
}
