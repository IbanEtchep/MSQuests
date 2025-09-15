package com.github.ibanetchep.msquests.core.lang;

public enum TranslationKey implements Translatable{

    CONFIG_RELOADED("config.reloaded"),
    QUEST_STARTED("quest.started"),
    QUEST_COULD_NOT_START("quest.could_not_start"),
    QUEST_FORCE_COMPLETED("quest.force_completed"),
    QUEST_LIST_HEADER("quest_list.header"),
    QUEST_LIST_GROUP("quest_list.group"),
    QUEST_LIST_QUEST("quest_list.quest"),
    QUEST_LIST_OBJECTIVE_IN_PROGRESS("quest_list.objective_in_progress"),
    QUEST_LIST_OBJECTIVE_COMPLETED("quest_list.objective_completed"),
    QUEST_OBJECTIVE_PROGRESS("quest.objective.progress");

    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}