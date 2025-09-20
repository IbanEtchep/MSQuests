package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.action.QuestAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BukkitQuestAction extends QuestAction {

    public BukkitQuestAction(QuestActionDTO rewardDto) {
        super(rewardDto);
    }

    protected Set<Player> getOnlinePlayers(Quest quest) {
        return quest.getActor().getProfiles().stream()
                .map(profile -> Bukkit.getPlayer(profile.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
