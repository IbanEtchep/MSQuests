package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum QuestObjectiveStatus implements Translatable {

    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ;

    @Override
    public String getTranslationKey() {
        return "objective.status." + name().toLowerCase();
    }

    public String getPrefixTranslationKey() {
        return "objective.status_prefix." + name().toLowerCase();
    }

    public String getSuffixTranslationKey() {
        return "objective.status_suffix." + name().toLowerCase();
    }
}
