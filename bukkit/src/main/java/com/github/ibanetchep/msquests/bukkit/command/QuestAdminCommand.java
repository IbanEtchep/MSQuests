package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.command.annotations.QuestActorType;
import com.github.ibanetchep.msquests.bukkit.lang.Lang;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

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
        sender.reply(group.getName());
        for (QuestConfig questConfig : group.getOrderedQuests()) {
            Quest lastQuest = questRegistry.getLastQuest(actor, questConfig);
            sender.reply(questConfig.getName() + " (" + lastQuest.getStatus() + ")");
        }
    }

}
