package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class QuestActorParameterType implements ParameterType<BukkitCommandActor, QuestActor> {

    private final MSQuestsPlugin plugin;

    public QuestActorParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public QuestActor parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();
        String actorType = executionContext.getResolvedArgument("actor type");

        return plugin.getQuestActorRegistry().getActorsByType(actorType).stream()
                .filter(actor -> actor.getName().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> {
            String[] args = context.input().source().split(" ");
            String actorType = args[2];
            return plugin.getQuestActorRegistry().getActorsByType(actorType).stream().map(QuestActor::getName).toList();
        };
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}