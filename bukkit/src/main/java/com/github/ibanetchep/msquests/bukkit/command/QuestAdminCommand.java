package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.logging.Level;

@Command("msquests")
@CommandPermission("msquests.admin")
public class QuestAdminCommand {

    private final MSQuestsPlugin plugin;

    public QuestAdminCommand(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @Description("Reloads the quest configs")
    public void reload(CommandSender sender) {
        plugin.loadConfig();
        plugin.getTranslator().load();

        plugin.getQuestConfigService()
                .loadQuestGroups()
                .thenCompose(v -> plugin.getQuestActorService().reloadActors())
                .thenRun(() -> sender.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_RELOAD).toComponent()))
                .exceptionally(e -> {
                    sender.sendMessage("<red>Failed to reload quests. Check console for details.</red>");
                    plugin.getLogger().log(Level.SEVERE, "Failed during quest reload", e);
                    return null;
                });
    }

    @Subcommand("actor <actor type> <actor> list <group>")
    public void listActorQuests(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            @Default("1") @Range(min = 1) @Named("page") int page
    ) {
        int lastPage = actor.getQuestsByGroupCount(group) / 10 + 1;

        if(page > lastPage) {
            sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS)
                    .placeholder("page", String.valueOf(page))
                    .placeholder("last_page", String.valueOf(lastPage))
                    .toComponent());
            return;
        }

        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_BODY)
                .applyPlaceholderResolver(group)
                .listPlaceholder(
                        "quests",
                        actor.getQuestsByGroup(group, page),
                        quest -> {
                            StringBuilder objectivesBuilder = new StringBuilder();
                            for (QuestObjective<?> obj : quest.getObjectives().values()) {
                                String rendered = MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_OBJECTIVE_TEMPLATE)
                                        .applyPlaceholderResolver(obj)
                                        .toStringRaw();
                                objectivesBuilder.append(rendered);
                            }

                            return MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_LIST_QUEST_TEMPLATE)
                                    .applyPlaceholderResolver(quest)
                                    .placeholder("objectives", objectivesBuilder.toString())
                                    .toStringRaw();
                        }
                )
                .placeholder("group_key", group.getKey())
                .placeholder("page", String.valueOf(page))
                .placeholder("next_page", String.valueOf(page + 1))
                .placeholder("previous_page", String.valueOf(page - 1))
                .placeholder("last_page", String.valueOf(lastPage))
                .placeholder("actor_type", actorType)
                .placeholder("actor", actor.getName())
                .toComponent()
        );
    }

    @Subcommand("actor <actor type> <actor> start <group> <quest config>")
    public void startActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            QuestConfig questConfig
    ) {
        boolean result = plugin.getQuestLifecycleService().startQuest(actor, questConfig);

        if(result) {
            sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_STARTED)
                    .applyPlaceholderResolver(questConfig)
                    .toComponent());
        } else {
            sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_COULD_NOT_START)
                    .applyPlaceholderResolver(questConfig)
                    .toComponent());
        }
    }

    @Subcommand("actor <actor type> <actor> complete <group> <quest>")
    public void completeActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroupConfig group,
            Quest quest
    ) {
        plugin.getQuestLifecycleService().completeQuest(quest);
        sender.reply(MessageBuilder.translatable(TranslationKey.QUEST_ADMIN_FORCE_COMPLETED)
                .applyPlaceholderResolver(quest)
                .toComponent());
    }

    /**
     * - /msquests actor <actortype> <actorId> remove <group_key> <quest_key>
     */
}
