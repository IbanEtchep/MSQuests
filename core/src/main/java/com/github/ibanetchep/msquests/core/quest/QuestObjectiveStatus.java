package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum QuestObjectiveStatus implements Translatable {

    IN_PROGRESS,
    COMPLETED,
    ;

    @Override
    public String getTranslationKey() {
        return "objective.status." + name().toLowerCase();
    }
}
