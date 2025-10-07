package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class QuestConfigParameterType implements ParameterType<BukkitCommandActor, QuestConfig> {

    private final MSQuestsPlugin plugin;

    public QuestConfigParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public QuestConfig parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();
        QuestGroupConfig questGroupConfig = executionContext.getResolvedArgument("group");
        QuestConfig questConfig = questGroupConfig.getQuestConfigs().get(value);

        if(questConfig == null) {
            throw new IllegalArgumentException("Could not find quest params " + value);
        }

        return questConfig;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> {
            QuestGroupConfig questGroupConfig = context.getResolvedArgument("group");

            return questGroupConfig.getQuestConfigs().keySet();
        };
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}