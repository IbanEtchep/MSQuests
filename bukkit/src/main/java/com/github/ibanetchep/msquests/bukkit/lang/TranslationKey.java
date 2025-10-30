package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.Translatable;

public enum TranslationKey implements Translatable {

    QUEST_ADMIN_RELOAD("quest_admin.reload"),
    QUEST_ADMIN_STARTED("quest_admin.quest.started"),
    QUEST_ADMIN_COULD_NOT_START("quest_admin.quest.could_not_start"),
    QUEST_ADMIN_FORCE_COMPLETED("quest_admin.quest.force_completed"),
    QUEST_ADMIN_LIST_BODY("quest_admin.list.body"),
    QUEST_ADMIN_LIST_QUEST_TEMPLATE("quest_admin.list.quest_template"),
    QUEST_ADMIN_LIST_OBJECTIVE_TEMPLATE("quest_admin.list.objective_template"),
    QUEST_ADMIN_LIST_PAGE_OUT_OF_BOUNDS("quest_admin.list.page_out_of_bounds"),
    QUEST_ADMIN_DISTRIBUTED_GROUP("quest_admin.group.distributed_quests"),
    QUEST_COMMAND_TRACKED("quest.command.tracked"),
    QUEST_COMMAND_UNTRACKED("quest.command.untracked"),
    PLACEHOLDER_QUEST("placeholder.quest"),
    PLACEHOLDER_STAGE("placeholder.stage"),
    PLACEHOLDER_OBJECTIVE("placeholder.quest");

    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}