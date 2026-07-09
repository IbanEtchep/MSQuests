package com.github.ibanetchep.msquests.core.quest.result;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum QuestRotateResult implements Translatable {

    SUCCESS,
    NOT_ROTATABLE,
    MAX_ROTATIONS_REACHED,
    QUEST_NOT_ACTIVE,
    NO_ALTERNATIVE_AVAILABLE,
    GROUP_NOT_FOUND;

    @Override
    public String getTranslationKey() {
        return "quest.rotate.result." + name().toLowerCase();
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}
