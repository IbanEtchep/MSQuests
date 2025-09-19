package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import org.bukkit.entity.Player;

public class PlayerPlaceholderResolver implements PlaceholderResolver<Player> {

    @Override
    public String resolve(String template, Player player) {
        return template
                .replace("%player%", player.getName());
    }

}
