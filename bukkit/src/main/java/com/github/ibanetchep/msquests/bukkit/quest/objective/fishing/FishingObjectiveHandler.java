package com.github.ibanetchep.msquests.bukkit.quest.objective.fishing;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingObjectiveHandler extends QuestObjectiveHandler<FishingObjective> implements Listener {

    private final BukkitQuestsPlugin plugin;

    public FishingObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.FISHING;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());
        Material caughtMaterial = caughtItem.getItemStack().getType();

        for (FishingObjective objective : getQuestObjectives(profile)) {
            if (objective.isCompleted()) continue;
            Material fishType = objective.getObjectiveConfig().getFishType();
            if (fishType == null || fishType == caughtMaterial) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }
    }
}
