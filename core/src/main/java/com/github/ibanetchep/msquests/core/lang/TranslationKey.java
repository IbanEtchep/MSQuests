package com.github.ibanetchep.msquests.core.lang;

public enum TranslationKey implements Translatable{

    CONFIG_RELOADED("messages.config.reloaded"),
    QUEST_STARTED("messages.quest.started"),
    QUEST_COULD_NOT_START("messages.quest.could_not_start"),
    QUEST_FORCE_COMPLETED("messages.quest.force_completed"),
    QUEST_LIST_HEADER("messages.quest_list.header"),
    QUEST_LIST_GROUP("messages.quest_list.group"),
    QUEST_LIST_QUEST("messages.quest_list.quest");

    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}