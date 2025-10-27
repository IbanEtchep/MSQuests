package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

public class ObjectivePlaceholderResolver implements PlaceholderResolver<QuestObjective> {

    @Override
    public String resolve(String template, QuestObjective objective) {
        return template
                .replace("%objective_progress%", Translator.raw(objective))
                .replace("%objective_progress_counter%", objective.getProgress() + "")
                .replace("%objective_target%", objective.getTarget() + "")
                .replace("%objective_name%", Translator.raw(objective.getObjectiveConfig()))
                .replace("%objective_status%", Translator.raw(objective.getStatus()))
                .replace("%objective_status_prefix%", Translator.raw(objective.getStatus().getPrefixTranslationKey()))
                .replace("%objective_status_suffix%", Translator.raw(objective.getStatus().getSuffixTranslationKey()))
                .replace("%objective_progress_percent%", String.valueOf(objective.getProgressPercentFormatted()));
    }

}
