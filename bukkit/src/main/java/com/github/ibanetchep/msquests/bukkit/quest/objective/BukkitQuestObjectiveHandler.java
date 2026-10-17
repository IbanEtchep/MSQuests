package com.github.ibanetchep.msquests.bukkit.quest.objective;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.List;
import java.util.Map;

public abstract class BukkitQuestObjectiveHandler<T extends QuestObjective> extends QuestObjectiveHandler<T> {

    protected final BukkitQuestsPlugin plugin;

    public BukkitQuestObjectiveHandler(BukkitQuestsPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public List<T> getEligibleObjectives(PlayerProfile profile) {
        if (profile == null) return List.of();
        return getQuestObjectives(profile).stream()
                .filter(obj -> !obj.isCompleted())
                .filter(obj -> meetsConditions(obj, profile))
                .toList();
    }

    private boolean meetsConditions(T objective, PlayerProfile profile) {
        if (objective.getObjectiveConfig().getConditions().isEmpty()) return true;
        Map<String, Object> args = Map.of(
                "quest", objective.getQuest().getQuestConfig().getKey(),
                "objective", objective.getObjectiveConfig().getKey());
        return plugin.getConditionChecker().test(objective.getObjectiveConfig().getConditions(), profile, args);
    }
}
