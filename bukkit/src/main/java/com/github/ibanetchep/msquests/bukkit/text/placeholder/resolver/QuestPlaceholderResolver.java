package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderEngine;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;

public class QuestPlaceholderResolver implements PlaceholderResolver<Quest> {

    @Override
    public String resolve(String template, Quest quest) {
        QuestConfig questConfig = quest.getQuestConfig();

        template = PlaceholderEngine.getInstance().apply(template, questConfig);
        template = PlaceholderEngine.getInstance().apply(template, quest.getActor());

        return template
                .replace("%quest_id%", quest.getId().toString())
                .replace("%quest_status%", Translator.raw(quest.getStatus()))
                .replace("%quest_status_prefix%", Translator.raw(quest.getStatus().getPrefixTranslationKey()))
                .replace("%quest_status_suffix%", Translator.raw(quest.getStatus().getSuffixTranslationKey()));
    }
}