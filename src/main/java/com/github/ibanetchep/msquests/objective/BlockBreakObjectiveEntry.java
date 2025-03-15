package com.github.ibanetchep.msquests.objective;

import com.github.ibanetchep.msquests.annotation.ConfigurationField;
import com.github.ibanetchep.msquests.annotation.ObjectiveEntryType;
import com.github.ibanetchep.msquests.model.QuestEntry;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;
import java.util.UUID;

@ObjectiveEntryType(type = "block_break")
public class BlockBreakObjectiveEntry extends QuestObjectiveEntry implements Listener {

    @ConfigurationField(required = true)
    private final Material blockType;

    @ConfigurationField(required = true)
    private final int amountToBreak;

    public BlockBreakObjectiveEntry(UUID uniqueId, QuestEntry quest, Integer progress, Map<String, String> config) {
        super(uniqueId, quest, progress, config);
        this.blockType = Material.valueOf(config.get("block_type"));
        this.amountToBreak = Integer.parseInt(config.get("amount_to_break"));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        QuestActor actor = quest.getActor();

        if (!actor.isActor(player)) {
            return;
        }

        Material material = event.getBlock().getType();
        if (material == blockType && !isCompleted()) {
            progress++;
            callOnProgress();

            if (isCompleted()) {
                callOnComplete();
            }
        }
    }

    @Override
    public boolean isCompleted() {
        return getProgress() >= amountToBreak;
    }
}
