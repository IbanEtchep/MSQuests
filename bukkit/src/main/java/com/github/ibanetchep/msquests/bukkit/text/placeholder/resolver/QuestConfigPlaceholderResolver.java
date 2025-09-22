package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;

public class QuestConfigPlaceholderResolver implements PlaceholderResolver<QuestConfig> {

    @Override
    public String resolve(String template, QuestConfig questConfig) {
        return template
                .replace("%quest_name%", questConfig.getName())
                .replace("%quest_description%", questConfig.getDescription() != null ? questConfig.getDescription() : "")
                .replace("%quest_key%", questConfig.getKey());
    }
}