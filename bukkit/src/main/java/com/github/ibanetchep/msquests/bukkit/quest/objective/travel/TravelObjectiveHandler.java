package com.github.ibanetchep.msquests.bukkit.quest.objective.travel;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TravelObjectiveHandler extends BukkitQuestObjectiveHandler<TravelObjective> implements Listener {

    public TravelObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.TRAVEL;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!changedBlock(event)) return;
        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (TravelObjective objective : getEligibleObjectives(profile)) {
            plugin.getQuestProgressService().progressObjective(objective, 1, profile);
        }
    }

    /** Paper has {@code PlayerMoveEvent#hasChangedBlock}; Spigot does not. */
    private static boolean changedBlock(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        return to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ() || from.getWorld() != to.getWorld());
    }
}
