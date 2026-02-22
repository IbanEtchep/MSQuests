package com.github.ibanetchep.msquests.bukkit.quest.objective.executecommand;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.PlayerQuestHandleEvent;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.bukkit.quest.objective.deliveritem.DeliverItemObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ExecuteCommandObjectiveHandler extends QuestObjectiveHandler<ExecuteCommandObjective> implements Listener {

    private final BukkitQuestsPlugin plugin;

    public ExecuteCommandObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.EXECUTE_COMMAND;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (ExecuteCommandObjective objective : getQuestObjectives(profile)) {
            if (objective.isCompleted()) {
                continue;
            }

            if (event.getMessage().startsWith("/" + objective.getObjectiveConfig().getCommand())) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }

    }

}