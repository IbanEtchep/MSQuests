package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderEngine;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * Executes a command for each online player that is the actor of the quest.
 */
public class PlayerCommandAction extends BukkitQuestAction {

    private final String commandTemplate;

    public PlayerCommandAction(QuestActionDTO dto) {
        super(dto);
        this.commandTemplate = (String) dto.config().get("command");
    }

    @Override
    public void execute(Quest quest) {
        String command = PlaceholderEngine.getInstance().apply(commandTemplate, quest);

        getOnlinePlayers(quest).forEach(player -> {
            if(quest.getActor().isMember(player.getUniqueId())) {
                String playerCommand = PlaceholderEngine.getInstance().apply(command, player);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
            }
        });
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
