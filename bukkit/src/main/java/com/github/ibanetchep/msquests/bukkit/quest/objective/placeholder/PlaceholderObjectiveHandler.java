package com.github.ibanetchep.msquests.bukkit.quest.objective.placeholder;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.objective.BukkitQuestObjectiveHandler;
import com.github.ibanetchep.msquests.bukkit.quest.objective.ObjectiveTypes;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PlaceholderObjectiveHandler extends BukkitQuestObjectiveHandler<PlaceholderObjective> {

    private WrappedTask task;

    public PlaceholderObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getObjectiveType() {
        return ObjectiveTypes.PLACEHOLDER;
    }

    @Override
    public void init() {
        task = plugin.getScheduler().runTimer(this::checkPlaceholders, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        if (task != null) {
            task.cancel();
        }
    }

    private void checkPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = getPlayerProfile(player.getUniqueId());
            if (profile == null) continue;

            for (PlaceholderObjective objective : getEligibleObjectives(profile)) {
                String placeholder = "%" + objective.getObjectiveConfig().getPlaceholder() + "%";
                String resolved = PlaceholderAPI.setPlaceholders(player, placeholder);

                if (objective.getObjectiveConfig().evaluateCondition(resolved)) {
                    plugin.getQuestProgressService().progressObjective(objective, 1, profile);
                }
            }
        }
    }
}
