package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEntityObjectiveHandler extends QuestObjectiveHandler<KillEntityObjective> implements Listener {

    public KillEntityObjectiveHandler(MSQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.KILL_ENTITY;
    }

    @EventHandler
    public void onKillEntity(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if(player == null) {
            return;
        }

        for (KillEntityObjective objective : getQuestObjectives(player.getUniqueId())) {
            QuestActor actor = objective.getQuest().getActor();

            if (!actor.isMember(player.getUniqueId())) {
                return;
            }

            EntityType entityType = entity.getType();
            if(entityType == objective.getObjectiveConfig().getEntityType() && !objective.isCompleted()) {
                updateProgress(objective, 1);
            }
        }
    }
}
