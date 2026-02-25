package com.github.ibanetchep.msquests.bukkit.quest.objective;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.List;

public abstract class BukkitQuestObjectiveHandler<T extends QuestObjective> extends QuestObjectiveHandler<T> {

    protected final BukkitQuestsPlugin plugin;

    public BukkitQuestObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public List<T> getEligibleObjectives(PlayerProfile profile) {
        return getQuestObjectives(profile).stream()
                .filter(obj -> !obj.isCompleted())
                .filter(obj -> obj.getObjectiveConfig().getConditions().stream().allMatch(c -> c.test(profile)))
                .toList();
    }
}
