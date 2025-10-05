package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum TranslationKey implements Translatable {

    //QUEST GENERAL
    QUEST_ADMIN_RELOAD("quest_admin.reload"),
    QUEST_ADMIN_STARTED("quest_admin.quest.started"),
    QUEST_ADMIN_COULD_NOT_START("quest_admin.quest.could_not_start"),
    QUEST_ADMIN_FORCE_COMPLETED("quest_admin.quest.force_completed"),
    QUEST_ADMIN_LIST_BODY("quest_admin.list.body"),
    QUEST_ADMIN_LIST_QUEST_TEMPLATE("quest_admin.list.quest_template"),
    QUEST_ADMIN_LIST_OBJECTIVE_TEMPLATE("quest_admin.list.objective_template"),
    QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS("quest_admin.list.page_out_of_bounds"),


    //QUEST START
    PLAYER_QUEST_STARTED_BODY("player_quest.start.message.body"),
    PLAYER_QUEST_STARTED_OBJECTIVE("player_quest.start.message.objective"),
    PLAYER_QUEST_STARTED_TITLE("player_quest.start.title.title"),
    PLAYER_QUEST_STARTED_SUBTITLE("player_quest.start.title.subtitle"),
    PLAYER_QUEST_STARTED_ACTION_BAR("player_quest.start.action_bar"),

    GLOBAL_QUEST_STARTED_BODY("global_quest.start.message.body"),
    GLOBAL_QUEST_STARTED_OBJECTIVE("global_quest.start.message.objective"),
    GLOBAL_QUEST_STARTED_TITLE("global_quest.start.title.title"),
    GLOBAL_QUEST_STARTED_SUBTITLE("global_quest.start.title.subtitle"),
    GLOBAL_QUEST_STARTED_ACTION_BAR("global_quest.start.action_bar"),

    //QUEST COMPLETE
    PLAYER_QUEST_COMPLETED_BODY("player_quest.complete.message.body"),
    PLAYER_QUEST_COMPLETED_OBJECTIVE("player_quest.complete.message.objective"),
    PLAYER_QUEST_COMPLETED_TITLE("player_quest.complete.title.title"),
    PLAYER_QUEST_COMPLETED_SUBTITLE("player_quest.complete.title.subtitle"),
    PLAYER_QUEST_COMPLETED_ACTION_BAR("player_quest.complete.action_bar"),

    GLOBAL_QUEST_COMPLETED_BODY("global_quest.complete.message.body"),
    GLOBAL_QUEST_COMPLETED_OBJECTIVE("global_quest.complete.message.objective"),
    GLOBAL_QUEST_COMPLETED_TITLE("global_quest.complete.title.title"),
    GLOBAL_QUEST_COMPLETED_SUBTITLE("global_quest.complete.title.subtitle"),
    GLOBAL_QUEST_COMPLETED_ACTION_BAR("global_quest.complete.action_bar"),
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