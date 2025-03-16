package com.github.ibanetchep.msquests.model.quest.type;

import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.quest.Quest;
import com.github.ibanetchep.msquests.model.quest.QuestObjective;
import com.github.ibanetchep.msquests.model.quest.definition.BlockBreakObjectiveDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class BlockBreakObjective extends QuestObjective<BlockBreakObjectiveDefinition> implements Listener {

    public BlockBreakObjective(UUID id, Quest quest, int progress, BlockBreakObjectiveDefinition objectiveDefinition) {
        super(id, quest, progress, objectiveDefinition);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        QuestActor actor = quest.getActor();

        if (!actor.isActor(player)) {
            return;
        }

        Material material = event.getBlock().getType();
        if (material == objectiveDefinition.getBlockType() && !isCompleted()) {
            progress++;
            callOnProgress();

            if (isCompleted()) {
                callOnComplete();
            }
        }
    }

    @Override
    public boolean isCompleted() {
        return getProgress() >= objectiveDefinition.getAmountToBreak();
    }
}
