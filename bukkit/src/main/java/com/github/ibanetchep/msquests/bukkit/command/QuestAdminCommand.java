package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Map;
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
        plugin.getTranslator().load();

        plugin.getQuestPersistenceService()
                .loadQuestGroups()
                .thenCompose(v -> plugin.getQuestPersistenceService().reloadActorsQuests())
                .thenRun(() -> sender.sendMessage(Translator.t(TranslationKey.QUEST_ADMIN_RELOAD)))
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
            QuestGroup group,
            @Default("1") @Range(min = 1) @Named("page") int page
    ) {
        int lastPage = actor.getQuestsByGroupCount(group) / 10 + 1;

        if(page > lastPage) {
            sender.reply(Translator.t(TranslationKey.QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS, Map.of(
                    "page", String.valueOf(page), "last_page", String.valueOf(lastPage)
            )));
            return;
        }

        StringBuilder questsRaw = new StringBuilder();
        for(Quest quest : actor.getQuestsByGroup(group, page)) {
            StringBuilder objectivesRaw = new StringBuilder();

            for (QuestObjective<?> objective : quest.getObjectives().values()) {
                objectivesRaw.append(Translator.raw(TranslationKey.QUEST_ADMIN_LIST_OBJECTIVE, Map.of(
                        "objective", Translator.raw(objective),
                        "progress", objective.getProgress() + "",
                        "total", objective.getObjectiveConfig().getTargetAmount() + "",
                        "percentage", objective.getPercentage() + "%",
                        "status", Translator.raw(objective.getStatus()),
                        "status_symbol", Translator.raw(objective.getStatus().getSymbolTranslationKey())
                )));
            }

            questsRaw.append(Translator.raw(TranslationKey.QUEST_ADMIN_LIST_QUEST, Map.of("quest", quest.getQuestConfig().getName(),
                    "status", Translator.raw(quest.getStatus()),
                    "description", quest.getQuestConfig().getDescription(),
                    "objectives", objectivesRaw.toString()
            )));
        }

        sender.reply(Translator.t(TranslationKey.QUEST_ADMIN_LIST_BODY, Map.of(
                "group", group.getName(),
                "group_key", group.getKey(),
                "quests", questsRaw.toString(),
                "page", String.valueOf(page),
                "next_page", String.valueOf(page + 1),
                "previous_page", String.valueOf(page - 1),
                "last_page", String.valueOf(lastPage),
                "actor_type", actorType,
                "actor", actor.getName()
        )));
    }

    @Subcommand("actor <actor type> <actor> start <group> <quest config>")
    public void startActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroup group,
            QuestConfig questConfig
    ) {
        boolean result = plugin.getQuestLifecycleService().startQuest(actor, questConfig);

        if(result) {
            sender.reply(Translator.t(TranslationKey.QUEST_ADMIN_STARTED, Map.of("quest", questConfig.getName())));
        } else {
            sender.reply(Translator.t(TranslationKey.QUEST_ADMIN_COULD_NOT_START, Map.of("quest", questConfig.getName())));
        }
    }

    @Subcommand("actor <actor type> <actor> complete <group> <quest>")
    public void completeActorQuest(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroup group,
            Quest quest
    ) {
        plugin.getQuestLifecycleService().completeQuest(quest);
        sender.reply(Translator.t(TranslationKey.QUEST_ADMIN_FORCE_COMPLETED, Map.of("quest", quest.getQuestConfig().getName())));
    }

    /**
     * - /msquests actor <actortype> <actorId> remove <group_key> <quest_key>
     */
}
