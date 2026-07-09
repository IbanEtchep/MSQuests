package com.github.ibanetchep.msquests.bukkit.quest.objective.breedanimal;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class BreedAnimalObjectiveHandler extends BukkitQuestObjectiveHandler<BreedAnimalObjective> implements Listener {

    public BreedAnimalObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.BREED_ANIMAL;
    }

    @Override
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void destroy() {}

    @EventHandler(ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;
        PlayerProfile profile = getPlayerProfile(player.getUniqueId());
        EntityType bredType = event.getEntity().getType();

        for (BreedAnimalObjective objective : getEligibleObjectives(profile)) {
            EntityType entityType = objective.getObjectiveConfig().getEntityType();
            if (entityType == null || entityType == bredType) {
                plugin.getQuestProgressService().progressObjective(objective, 1, profile);
            }
        }
    }
}
