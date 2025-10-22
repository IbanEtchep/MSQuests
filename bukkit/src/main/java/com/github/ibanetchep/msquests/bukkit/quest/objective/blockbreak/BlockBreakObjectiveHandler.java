package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakObjectiveHandler extends QuestObjectiveHandler<BlockBreakObjective> implements Listener {

    private final MSQuestsPlugin plugin;

    public BlockBreakObjectiveHandler(MSQuestsPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.BLOCK_BREAK;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (BlockBreakObjective objective : getQuestObjectives(profile)) {
            Material material = event.getBlock().getType();
            if (material == objective.getObjectiveConfig().getMaterial() && !objective.isCompleted()) {
                plugin.getProgressManager().progressObjective(objective, 1, profile);
            }
        }
    }
}
