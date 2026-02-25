package com.github.ibanetchep.msquests.core.quest.condition;

import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

@FunctionalInterface
public interface QuestObjectiveCondition {
    boolean test(PlayerProfile profile);
}
