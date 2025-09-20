package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

public class GroupPlaceholderResolver implements PlaceholderResolver<QuestGroupConfig> {
    @Override
    public String resolve(String template, QuestGroupConfig group) {
        return template
                .replace("%group%", group.getName())
                .replace("%group_description%", group.getDescription() != null ? group.getDescription() : "");
    }
}
