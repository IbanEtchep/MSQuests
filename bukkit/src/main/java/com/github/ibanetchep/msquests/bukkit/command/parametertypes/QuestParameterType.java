package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class QuestParameterType implements ParameterType<BukkitCommandActor, Quest> {

    private final MSQuestsPlugin plugin;

    public QuestParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Quest parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();
        QuestActor questActor = executionContext.getResolvedArgument("actor");
        QuestGroup questGroup = executionContext.getResolvedArgument("group");
        Quest quest = questActor.getQuestByKey(value);

        if(quest == null || !quest.isActive()) {
            throw new IllegalArgumentException("Could not find quest " + value);
        }

        return quest;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> {
            QuestActor questActor = context.getResolvedArgument("actor");
            QuestGroup questGroup = context.getResolvedArgument("group");

            return questActor.getQuests().values().stream()
                    .filter(Quest::isActive)
                    .map(Quest::getQuestConfig)
                    .filter(questConfig -> questConfig.getGroup().equals(questGroup))
                    .map(QuestConfig::getKey)
                    .toList();
        };
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}