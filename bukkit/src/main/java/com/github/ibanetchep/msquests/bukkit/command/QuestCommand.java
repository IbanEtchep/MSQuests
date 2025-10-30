package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("quest")
@CommandPermission("msquests.player")
public class QuestCommand {

    private final MSQuestsPlugin plugin;

    public QuestCommand(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("track")
    @Description("Tracks a quest")
    public void track(Player sender, QuestGroupConfig group, Quest quest) {
        var playerProfile = plugin.getPlayerProfileRegistry().getPlayerProfile(sender.getUniqueId());

        if(playerProfile == null) return;

        playerProfile.setTrackedQuestId(quest.getId());
        plugin.getPlayerProfileService().saveProfile(playerProfile);

        sender.sendMessage(
                MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_TRACKED)
                        .applyPlaceholderResolver(quest).toComponent()
        );
    }

    @Subcommand("untrack")
    @Description("Untracks currently tracked quest.")
    public void track(Player sender) {
        var playerProfile = plugin.getPlayerProfileRegistry().getPlayerProfile(sender.getUniqueId());

        if(playerProfile == null) return;

        playerProfile.setTrackedQuestId(null);
        plugin.getPlayerProfileService().saveProfile(playerProfile);

        sender.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_UNTRACKED).toComponent());
    }

}
