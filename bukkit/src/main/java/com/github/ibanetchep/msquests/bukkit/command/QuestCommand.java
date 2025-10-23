package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.lang.TranslationKey;
import com.github.ibanetchep.msquests.bukkit.text.MessageBuilder;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.logging.Level;

@Command("quest")
@CommandPermission("msquests.player")
public class QuestCommand {

    private final MSQuestsPlugin plugin;

    public QuestCommand(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("track")
    @Description("Tracks a quest")
    public void reload(Player sender, Quest quest) {
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
}
