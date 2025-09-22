package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class QuestGroupParameterType implements ParameterType<BukkitCommandActor, QuestGroupConfig> {

    private final MSQuestsPlugin plugin;

    public QuestGroupParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public QuestGroupConfig parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();

        return plugin.getQuestConfigRegistry().getQuestGroupConfigs().get(value);
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> plugin.getQuestConfigRegistry().getQuestGroupConfigs().keySet();
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}