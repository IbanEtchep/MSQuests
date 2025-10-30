package com.github.ibanetchep.msquests.bukkit.command.parametertypes;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
import com.github.ibanetchep.msquests.bukkit.service.QuestPlayerService;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class QuestParameterType implements ParameterType<BukkitCommandActor, Quest> {

    private final MSQuestsPlugin plugin;

    public QuestParameterType(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Quest parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> executionContext) {
        String value = input.readString();
        QuestActor questActor = executionContext.getResolvedArgumentOrNull("actor");

        Player player = executionContext.actor().asPlayer();
        if(player != null && questActor == null) {
            questActor = plugin.getQuestActorRegistry().getActors().get(player.getUniqueId());
        }

        if(questActor == null) {
            throw new IllegalArgumentException("Player or Quest Actor not found");
        }

        QuestGroupConfig group = executionContext.getResolvedArgument("group");
        ActorQuestGroup actorQuestGroup = questActor.getActorQuestGroup(group);

        if(actorQuestGroup == null) {
            throw new IllegalArgumentException("Actor is not part of the given group");
        }

        Quest quest = actorQuestGroup.getActiveQuestByKey(value);

        if(quest == null) {
            throw new IllegalArgumentException("Could not find quest " + value);
        }

        return quest;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return (context) -> {
            QuestActor questActor = context.getResolvedArgumentOrNull("actor");
            QuestGroupConfig questGroupConfig = context.getResolvedArgumentOrNull("group");

            Player player = context.actor().asPlayer();
            if(player != null && questActor == null) {
                questActor = plugin.getQuestActorRegistry().getActors().get(player.getUniqueId());
            }

            if(questActor == null) {
                return List.of();
            }

            Stream<QuestConfig> actorQuests = questActor.getQuests().values().stream()
                    .filter(Quest::isActive)
                    .map(Quest::getQuestConfig);

            if(questGroupConfig != null) {
                actorQuests = actorQuests
                        .filter(questConfig -> questConfig.getGroupConfig().equals(questGroupConfig));
            }

            return actorQuests.map(QuestConfig::getKey).toList();
        };
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}