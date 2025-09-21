package com.github.ibanetchep.msquests.bukkit.text.placeholder.resolver;

import com.github.ibanetchep.msquests.bukkit.lang.Translator;
import com.github.ibanetchep.msquests.bukkit.text.placeholder.PlaceholderResolver;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

public class ObjectivePlaceholderResolver implements PlaceholderResolver<QuestObjective<?>> {

    @Override
    public String resolve(String template, QuestObjective<?> objective) {
        return template
                .replace("%objective_name%", Translator.raw(objective))
                .replace("%objective_status%", Translator.raw(objective.getStatus()))
                .replace("%objective_status_symbol%", Translator.raw(objective.getStatus().getSymbolTranslationKey()))
                .replace("%objective_progress%", String.valueOf(objective.getProgress()))
                .replace("%objective_progress_percent%", String.valueOf(objective.getProgressPercent()))
                .replace("%objective_total%", String.valueOf(objective.getObjectiveConfig().getTargetAmount()));
    }

}
