package com.github.ibanetchep.msquests.bukkit.quest.objective.killentity;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEntityObjectiveHandler extends BukkitQuestObjectiveHandler<KillEntityObjective> implements Listener {

    public KillEntityObjectiveHandler(BukkitQuestsPlugin plugin) {
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

        PlayerProfile profile = getPlayerProfile(player.getUniqueId());

        for (KillEntityObjective objective : getEligibleObjectives(profile)) {
            if (entity.getType() == objective.getObjectiveConfig().getEntityType()) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }
    }
}
