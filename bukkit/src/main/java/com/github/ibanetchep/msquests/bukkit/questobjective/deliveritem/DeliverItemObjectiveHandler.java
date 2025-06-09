package com.github.ibanetchep.msquests.bukkit.questobjective.deliveritem;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.event.PlayerQuestHandleEvent;
import com.github.ibanetchep.msquests.bukkit.questobjective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeliverItemObjectiveHandler extends QuestObjectiveHandler<DeliverItemObjective> implements Listener {

    public DeliverItemObjectiveHandler(MSQuestsPlugin plugin) {
        super(plugin.getQuestManager());
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.DELIVER_ITEM;
    }

    @EventHandler
    public void onHandle(PlayerQuestHandleEvent event) {
        Player player = event.getPlayer();

        for (DeliverItemObjective objective : getQuestObjectives(player.getUniqueId())) {
            // Check if player has item
        }
    }

}
