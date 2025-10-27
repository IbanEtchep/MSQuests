package com.github.ibanetchep.msquests.core.quest.result;

import com.github.ibanetchep.msquests.core.lang.Translatable;

/**
 * Represents the result of attempting to start a quest.
 * Contains success status and optional failure reason.
 */
public enum QuestStartResult implements Translatable {

    SUCCESS,
    INVALID_ACTOR_TYPE,
    GROUP_NOT_FOUND,
    GROUP_INACTIVE,
    ALREADY_ACTIVE,
    ALREADY_COMPLETED,
    MAX_ACTIVE_REACHED,
    PERIOD_LIMIT_REACHED,
    CANCELLED_BY_EVENT;

    @Override
    public String getTranslationKey() {
        return "quest.start.result." + name().toLowerCase();
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}