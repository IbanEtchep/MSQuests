package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.lang.Lang;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;
import java.util.Map;

@Command("msquests")
@CommandPermission("msquests.admin")
public class QuestAdminCommand {

    private final MSQuestsPlugin plugin;
    private final QuestRegistry questRegistry;

    public QuestAdminCommand(MSQuestsPlugin plugin) {
        this.plugin = plugin;
        this.questRegistry = plugin.getQuestRegistry();
    }

    @Subcommand("reload")
    @Description("Reloads the quest configs")
    public void reload(CommandSender sender) {
        plugin.getLangManager().load();
        plugin.getQuestPersistenceService().loadQuestGroups();
        sender.sendMessage(Lang.CONFIG_RELOADED.component());
    }

    /**
     * - /msquests actor <actortype> <actorId> list <group_key>
     * - /msquests actor <actortype> <actorId> start <group_key> <quest_key>
     * - /msquests actor <actortype> <actorId> complete <group_key> <quest_key>
     * - /msquests actor <actortype> <actorId> remove <group_key> <quest_key>
     */

    @Subcommand("actor <actor type> <actor> list <group>")
    public void listActorQuests(
            BukkitCommandActor sender,
            @QuestActorType String actorType,
            QuestActor actor,
            QuestGroup group
    ) {
        Map<QuestGroup, List<Quest>> questsByGroup = actor.getQuestsByGroup();

        sender.reply(Lang.QUEST_LIST_HEADER.component());
        for(QuestGroup questGroup : questsByGroup.keySet()) {
            sender.reply(Lang.QUEST_LIST_GROUP.component("group", questGroup.getName()));
            for(Quest quest : questsByGroup.get(questGroup)) {
                sender.reply(Lang.QUEST_LIST_QUEST.component(
                        "quest", quest.getQuestConfig().getName(),
                        "status", quest.getStatus().toString(),
                        "description", quest.getQuestConfig().getDescription()
                ));
            }
        }
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
            sender.reply(Lang.QUEST_STARTED.component("quest", questConfig.getName()
            ));
        } else {
            sender.reply(Lang.QUEST_COULD_NOT_START.component("quest", questConfig.getName()));
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
        sender.reply(Lang.QUEST_FORCE_COMPLETED.component());
    }
}
