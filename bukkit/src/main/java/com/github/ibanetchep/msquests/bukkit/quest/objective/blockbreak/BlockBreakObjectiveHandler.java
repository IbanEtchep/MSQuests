package com.github.ibanetchep.msquests.bukkit.quest.objective.blockbreak;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
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
        super(plugin.getQuestManager());
        this.plugin = plugin;
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.BLOCK_BREAK;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        for (BlockBreakObjective objective : getQuestObjectives(player.getUniqueId())) {
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
