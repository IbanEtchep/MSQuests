package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BukkitQuestAction extends QuestAction {

    protected final MSQuestsPlugin plugin;

    public BukkitQuestAction(QuestActionDTO rewardDto, MSQuestsPlugin plugin) {
        super(rewardDto);
        this.plugin = plugin;
    }

    protected Set<Player> getOnlinePlayers(Quest quest) {
        return quest.getActor().getProfiles().stream()
                .map(profile -> Bukkit.getPlayer(profile.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
