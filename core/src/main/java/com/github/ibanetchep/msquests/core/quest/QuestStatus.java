package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum QuestStatus implements Translatable {

    IN_PROGRESS,
    COMPLETED,
    FAILED;

    @Override
    public String getTranslationKey() {
        return "quest.status." + name().toLowerCase();
    }

    public String getPrefixTranslationKey() {
        return "objective.status_prefix." + name().toLowerCase();
    }

    public String getSuffixTranslationKey() {
        return "objective.status_suffix." + name().toLowerCase();
    }
}
