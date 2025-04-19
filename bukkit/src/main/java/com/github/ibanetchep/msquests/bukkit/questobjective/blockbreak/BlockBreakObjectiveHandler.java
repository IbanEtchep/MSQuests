package com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakObjectiveHandler extends QuestObjectiveHandler<BlockBreakObjective> implements Listener {

    private final MSQuestsPlugin plugin;

    public BlockBreakObjectiveHandler(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getType() {
        return "block_break";
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        for (BlockBreakObjective objective : getQuestObjectives()) {
            QuestActor actor = objective.getQuest().getActor();

            if (!actor.isActor(player.getUniqueId())) {
                return;
            }

            Material material = event.getBlock().getType();
            if (material == objective.getObjectiveConfig().getBlockType() && !objective.isCompleted()) {
                updateProgress(objective, 1);
            }
        }
    }

}
