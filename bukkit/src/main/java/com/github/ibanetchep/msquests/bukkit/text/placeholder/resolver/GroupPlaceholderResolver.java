package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;

public class GroupPlaceholderResolver implements PlaceholderResolver<QuestGroup> {
    @Override
    public String resolve(String template, QuestGroup group) {
        return template
                .replace("%group%", group.getName())
                .replace("%group_description%", group.getDescription() != null ? group.getDescription() : "");
    }
}
