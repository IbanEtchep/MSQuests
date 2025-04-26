package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak.BlockBreakObjectiveConfig;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;

@Command("msquests")
@CommandPermission("msquests.admin")
public class QuestAdminCommand {

    private final MSQuestsPlugin plugin;

    public QuestAdminCommand(MSQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("createtest")
    @Description("Creates a test quest config with two block break objectives")
    public void createTest(CommandSender sender) {
        // Create quest config
        QuestConfig questConfig = new QuestConfig(
                "test_quest",
                "Test Quest",
                "A test quest with block breaking objectives",
                3600 // 1 hour duration
        );

        // Set tags
        Set<String> tags = new HashSet<>();
        tags.add("test");
        questConfig.setTags(tags);

        // Create first objective - Break 5 dirt blocks
        Map<String, Object> config1 = new HashMap<>();
        config1.put("name", "Break dirt");
        config1.put("description", "Break 5 dirt blocks");
        config1.put("block_type", Material.DIRT.name());
        config1.put("amount_to_break", "5");
        BlockBreakObjectiveConfig objective1 = new BlockBreakObjectiveConfig("break_dirt", config1);

        // Create second objective - Break 3 stone blocks
        Map<String, Object> config2 = new HashMap<>();
        config2.put("name", "Break stone");
        config2.put("description", "Break 3 stone blocks");
        config2.put("block_type", Material.STONE.name());
        config2.put("amount_to_break", "3");
        BlockBreakObjectiveConfig objective2 = new BlockBreakObjectiveConfig("break_stone", config2);

        questConfig.addObjective(objective1);
        questConfig.addObjective(objective2);

        plugin.getQuestManager().saveQuestConfig(questConfig);
        
        sender.sendMessage("§aTest quest config created with ID: " + questConfig.getKey());
    }

    @Subcommand("reload")
    @Description("Reloads the quest configs")
    public void reload(CommandSender sender) {
        plugin.getLangManager().load();
        plugin.getQuestManager().loadQuestConfigs();
        sender.sendMessage("§aQuest configs reloaded");
    }
}
