package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderEngine;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.logging.Level;

/**
 * Reward that executes a command once.
 */
@ActionType("command")
public class CommandAction extends BukkitQuestAction {

    @ConfigField(name = "command", required = true)
    private final String commandTemplate;

    public CommandAction(QuestActionDTO dto, MSQuestsPlugin plugin) {
        super(dto, plugin);
        this.commandTemplate = (String) dto.config().get("command");
    }

    @Override
    public void execute(Quest quest) {
        String command = PlaceholderEngine.getInstance().apply(commandTemplate, quest);

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to execute quest action command " + command, e);
        }
    }

    @Override
    public QuestActionDTO toDTO() {
        return new QuestActionDTO(getType(), getName(), Map.of("command", commandTemplate));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "command", commandTemplate
        );
    }
}
