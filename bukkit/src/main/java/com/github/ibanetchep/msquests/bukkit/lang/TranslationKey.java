package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum TranslationKey implements Translatable {

    QUEST_ADMIN_RELOAD("quest_admin.reload"),
    QUEST_ADMIN_STARTED("quest_admin.quest.started"),
    QUEST_ADMIN_COULD_NOT_START("quest_admin.quest.could_not_start"),
    QUEST_ADMIN_FORCE_COMPLETED("quest_admin.quest.force_completed"),
    QUEST_ADMIN_LIST_BODY("quest_admin.list.body"),
    QUEST_ADMIN_LIST_QUEST("quest_admin.list.quest"),
    QUEST_ADMIN_LIST_OBJECTIVE("quest_admin.list.objective"),
    QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS("quest_admin.list.page_out_of_bounds"),


    //QUEST START
    PLAYER_QUEST_STARTED_BODY("player_quest.started.message.body"),
    PLAYER_QUEST_STARTED_OBJECTIVE("player_quest.started.message.objective"),
    PLAYER_QUEST_STARTED_TITLE("player_quest.started.title.title"),
    PLAYER_QUEST_STARTED_SUBTITLE("player_quest.started.title.subtitle"),
    PLAYER_QUEST_STARTED_ACTION_BAR("player_quest.started.action_bar"),

    GLOBAL_QUEST_STARTED_BODY("global_quest.started.message.body"),
    GLOBAL_QUEST_STARTED_OBJECTIVE("global_quest.started.message.objective"),
    GLOBAL_QUEST_STARTED_TITLE("global_quest.started.title.title"),
    GLOBAL_QUEST_STARTED_SUBTITLE("global_quest.started.title.subtitle"),
    GLOBAL_QUEST_STARTED_ACTION_BAR("global_quest.started.action_bar"),

    //QUEST COMPLETE
    PLAYER_QUEST_COMPLETED_BODY("player_quest.completed.message.body"),
    PLAYER_QUEST_COMPLETED_OBJECTIVE("player_quest.completed.message.objective"),
    PLAYER_QUEST_COMPLETED_TITLE("player_quest.completed.title.title"),
    PLAYER_QUEST_COMPLETED_SUBTITLE("player_quest.completed.title.subtitle"),
    PLAYER_QUEST_COMPLETED_ACTION_BAR("player_quest.completed.action_bar"),

    GLOBAL_QUEST_COMPLETED_BODY("global_quest.completed.message.body"),
    GLOBAL_QUEST_COMPLETED_OBJECTIVE("global_quest.completed.message.objective"),
    GLOBAL_QUEST_COMPLETED_TITLE("global_quest.completed.title.title"),
    GLOBAL_QUEST_COMPLETED_SUBTITLE("global_quest.completed.title.subtitle"),
    GLOBAL_QUEST_COMPLETED_ACTION_BAR("global_quest.completed.action_bar"),
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