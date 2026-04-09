package com.github.ibanetchep.msquests.bukkit.quest.objective.harvestcrop;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class HarvestCropObjectiveHandler extends BukkitQuestObjectiveHandler<HarvestCropObjective> implements Listener {

    public HarvestCropObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.HARVEST_CROP;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (plugin.getBlockPlaceTagListener().isPlaced(block)) {
            return;
        }

        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (HarvestCropObjective objective : getEligibleObjectives(profile)) {
            HarvestCropObjectiveConfig config = objective.getObjectiveConfig();

            if (block.getType() != config.getCrop()) {
                continue;
            }

            if (config.requiresAgeCheck()) {
                if (!(block.getBlockData() instanceof Ageable ageable)) {
                    continue;
                }
                if (ageable.getAge() != ageable.getMaximumAge()) {
                    continue;
                }
            }

            plugin.getQuestProgressService().progressObjective(objective, 1, profile);
        }
    }
}
