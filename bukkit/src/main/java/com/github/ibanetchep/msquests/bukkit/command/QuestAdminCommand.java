package com.github.ibanetchep.msquests.bukkit.command;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.quest.QuestDefinition;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveDefinition;
import com.github.ibanetchep.msquests.bukkit.questobjective.blockbreak.BlockBreakObjectiveDefinition;
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
    @Description("Creates a test quest definition with two block break objectives")
    public void createTest(CommandSender sender) {
        // Create quest definition
        UUID questId = UUID.randomUUID();
        QuestDefinition questDefinition = new QuestDefinition(
                questId,
                "Test Quest",
                "A test quest with block breaking objectives",
                3600 // 1 hour duration
        );

        // Set tags
        Set<String> tags = new HashSet<>();
        tags.add("test");
        questDefinition.setTags(tags);

        // Create first objective - Break 5 dirt blocks
        Map<String, String> config1 = new HashMap<>();
        config1.put("name", "Break dirt");
        config1.put("description", "Break 5 dirt blocks");
        config1.put("block_type", Material.DIRT.name());
        config1.put("amount_to_break", "5");
        BlockBreakObjectiveDefinition objective1 = new BlockBreakObjectiveDefinition(config1);

        // Create second objective - Break 3 stone blocks
        Map<String, String> config2 = new HashMap<>();
        config2.put("name", "Break stone");
        config2.put("description", "Break 3 stone blocks");
        config2.put("block_type", Material.STONE.name());
        config2.put("amount_to_break", "3");
        BlockBreakObjectiveDefinition objective2 = new BlockBreakObjectiveDefinition(config2);

        // Add objectives to quest definition
        Map<UUID, QuestObjectiveDefinition> objectives = new HashMap<>();
        objectives.put(UUID.randomUUID(), objective1);
        objectives.put(UUID.randomUUID(), objective2);
        questDefinition.setObjectives(objectives);

        plugin.getQuestManager().saveQuestDefinition(questDefinition);
        
        sender.sendMessage("§aTest quest definition created with ID: " + questId);
    }
}
