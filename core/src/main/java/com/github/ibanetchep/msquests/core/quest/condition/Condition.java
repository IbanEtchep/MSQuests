package com.github.ibanetchep.msquests.core.quest.condition;

import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.Map;

public interface Condition {
    boolean test(PlayerProfile profile);

    default boolean test(PlayerProfile profile, Map<String, String> contextPlaceholders) {
        return test(profile);
    }
}
