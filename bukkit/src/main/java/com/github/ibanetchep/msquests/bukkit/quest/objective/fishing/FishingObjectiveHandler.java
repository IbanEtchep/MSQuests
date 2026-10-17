package com.github.ibanetchep.msquests.bukkit.quest.objective.fishing;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingObjectiveHandler extends BukkitQuestObjectiveHandler<FishingObjective> implements Listener {

    public FishingObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.FISHING;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());
        ItemStack caught = caughtItem.getItemStack();

        for (FishingObjective objective : getEligibleObjectives(profile)) {
            if (objective.getObjectiveConfig().getItem().test(caught, plugin.getItems())) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }
    }
}
