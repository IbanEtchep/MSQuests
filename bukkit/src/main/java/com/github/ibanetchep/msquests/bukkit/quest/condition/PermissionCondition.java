package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class PermissionCondition implements Condition {

    private final String permission;

    public PermissionCondition(Map<String, Object> params) {
        this.permission = (String) params.get("permission");
    }

    @Override
    public boolean test(PlayerProfile profile) {
        Player player = Bukkit.getPlayer(profile.getId());
        return player != null && player.hasPermission(permission);
    }
}
