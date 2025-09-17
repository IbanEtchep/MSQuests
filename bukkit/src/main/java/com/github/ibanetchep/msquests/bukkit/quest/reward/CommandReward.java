package com.github.ibanetchep.msquests.bukkit.quest.reward;

import com.github.ibanetchep.msquests.core.dto.RewardDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.reward.Reward;
import org.bukkit.Bukkit;

import java.util.Map;

public class CommandReward extends Reward {

    private final String command;

    public CommandReward(RewardDTO dto) {
        super(dto);
        this.command = (String) dto.config().get("command");
    }

    @Override
    public void give(QuestActor actor) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%actor%", actor.getName()));
    }

    public String getCommand() {
        return command;
    }

    @Override
    public RewardDTO toDTO() {
        return new RewardDTO(getType(), getName(), Map.of("command", command));
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "command", command
        );
    }
}
