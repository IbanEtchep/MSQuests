package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("quest")
@CommandPermission("msquests.player")
public class QuestCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("track")
    @Description("Tracks a quest")
    public void track(Player sender, QuestGroupConfig group, Quest quest) {
        var playerProfile = plugin.getPlayerProfileRegistry().getPlayerProfile(sender.getUniqueId());

        if(playerProfile == null) return;

        playerProfile.setTrackedQuestId(quest.getId());
        plugin.getPlayerProfileService().saveProfile(playerProfile);

        if (plugin.getTrackingBossBarService().isEnabled()) {
            plugin.getTrackingBossBarService().showBossBar(sender);
        }

        sender.sendMessage(
                MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_TRACKED)
                        .applyPlaceholderResolver(quest).toComponent()
        );
    }

    @Subcommand("rotate")
    @Description("Rotates a quest for a new random one from the same group")
    @CommandPermission("msquests.rotate")
    public void rotate(Player sender, QuestGroupConfig group, Quest quest) {
        QuestActor actor = plugin.getQuestActorRegistry().getActors().get(sender.getUniqueId());
        if (actor == null) return;

        QuestRotateResult result = plugin.getQuestLifecycleService().rotateQuest(actor, quest);
        sender.sendMessage(MessageBuilder.translatable(result).toComponent());
    }

    @Subcommand("untrack")
    @Description("Untracks currently tracked quest.")
    public void track(Player sender) {
        var playerProfile = plugin.getPlayerProfileRegistry().getPlayerProfile(sender.getUniqueId());

        if(playerProfile == null) return;

        if (plugin.getTrackingBossBarService().isEnabled()) {
            plugin.getTrackingBossBarService().hideBossBar(sender);
        }

        playerProfile.setTrackedQuestId(null);
        plugin.getPlayerProfileService().saveProfile(playerProfile);

        sender.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_UNTRACKED).toComponent());
    }

    @Subcommand("toggletrack")
    @Description("Toggles tracking for a quest")
    public void toggleTrack(Player sender, QuestGroupConfig group, Quest quest) {
        var playerProfile = plugin.getPlayerProfileRegistry().getPlayerProfile(sender.getUniqueId());
        if (playerProfile == null) return;

        if (quest.getId().equals(playerProfile.getTrackedQuestId())) {
            if (plugin.getTrackingBossBarService().isEnabled()) {
                plugin.getTrackingBossBarService().hideBossBar(sender);
            }
            playerProfile.setTrackedQuestId(null);
            plugin.getPlayerProfileService().saveProfile(playerProfile);
            sender.sendMessage(MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_UNTRACKED).toComponent());
        } else {
            playerProfile.setTrackedQuestId(quest.getId());
            plugin.getPlayerProfileService().saveProfile(playerProfile);
            if (plugin.getTrackingBossBarService().isEnabled()) {
                plugin.getTrackingBossBarService().showBossBar(sender);
            }
            sender.sendMessage(
                    MessageBuilder.translatable(TranslationKey.QUEST_COMMAND_TRACKED)
                            .applyPlaceholderResolver(quest).toComponent()
            );
        }
    }

}
