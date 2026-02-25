package com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.PlayerQuestHandleEvent;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ExecuteCommandObjectiveHandler extends BukkitQuestObjectiveHandler<ExecuteCommandObjective> implements Listener {

    public ExecuteCommandObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.EXECUTE_COMMAND;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (ExecuteCommandObjective objective : getEligibleObjectives(profile)) {
            if (event.getMessage().startsWith("/" + objective.getObjectiveConfig().getCommand())) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }

    }

}