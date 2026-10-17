package com.github.ibanetchep.msquests.database;

/**
 * Table names of one storage layout. {@link #LEGACY} is what MSQuests has always written;
 * {@link #ARTISAN} is used once quests are read from {@code quest_group} Documents
 * ({@code content_source: artisan}), where rows reference uids instead of editable keys.
 *
 * @param rotationGroupColumn column naming the group of a rotation: its key, or its uid
 */
public record SqlTables(String profile, String actor, String quest, String objective, String rotation,
                        String rotationGroupColumn) {

    public static final SqlTables LEGACY = new SqlTables(
            "msquests_player_profile", "msquests_actor", "msquests_quest", "msquests_objective",
            "msquests_rotation", "group_key");

    public static final SqlTables ARTISAN = new SqlTables(
            "artisan_quests_profile", "artisan_quests_actor", "artisan_quests_quest", "artisan_quests_objective",
            "artisan_quests_rotation", "group_uid");
}
