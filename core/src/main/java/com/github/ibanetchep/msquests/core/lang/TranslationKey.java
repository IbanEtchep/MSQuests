package com.github.ibanetchep.msquests.core.lang;

public enum TranslationKey implements Translatable{

    QUEST_ADMIN_RELOAD("quest_admin.reload"),
    QUEST_ADMIN_STARTED("quest_admin.quest.started"),
    QUEST_ADMIN_COULD_NOT_START("quest_admin.quest.could_not_start"),
    QUEST_ADMIN_FORCE_COMPLETED("quest_admin.quest.force_completed"),
    QUEST_ADMIN_LIST_HEADER("quest_admin.list.header"),
    QUEST_ADMIN_LIST_BODY("quest_admin.list.body"),
    QUEST_ADMIN_LIST_QUEST("quest_admin.list.quest"),
    QUEST_ADMIN_LIST_OBJECTIVE("quest_admin.list.objective"),
    PLAYER_QUEST_STARTED_BODY("player_quest.started.body"),
    PLAYER_QUEST_STARTED_OBJECTIVE("player_quest.started.objective"),
    GLOBAL_QUEST_STARTED_BODY("global_quest.started.body"),
    GLOBAL_QUEST_STARTED_OBJECTIVE("global_quest.started.objective"),
    ;

    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}