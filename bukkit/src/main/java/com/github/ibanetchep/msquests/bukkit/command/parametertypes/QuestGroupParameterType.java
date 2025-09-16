package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class QuestGroupParameterType implements ParameterType<BukkitCommandActor, QuestGroup> {

    private final MSQuestsPlugin plugin;

    public QuestGroupParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public QuestGroup parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();

        return plugin.getQuestRegistry().getQuestGroups().get(value);
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> plugin.getQuestRegistry().getQuestGroups().keySet();
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}