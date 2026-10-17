package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.condition.ConditionChecker;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** A quest's {@code requirements} are Artisan conditions, tested through the {@link ConditionChecker} port. */
class QuestDistributionRequirementsTest {

    private final List<Map<String, Object>> seenArgs = new ArrayList<>();
    private QuestGroupConfig group;
    private QuestConfig quest;
    private QuestActor actor;
    private PlayerProfile profile;

    @BeforeEach
    void setUp() {
        group = new QuestGroupConfig.Builder("daily", "Daily", "", "player").maxActive(5).build();
        quest = new QuestConfig("mine", "Mine", "", 0);
        group.addQuest(quest);
        UUID id = UUID.randomUUID();
        actor = new QuestActor(id, "Steve") {
            @Override
            public String getActorType() {
                return "player";
            }

            @Override
            public boolean isMember(UUID playerId) {
                return id.equals(playerId);
            }
        };
        profile = new PlayerProfile(id, "Steve");
    }

    private QuestDistributionService service(boolean met) {
        return new QuestDistributionService((conditions, p, args) -> {
            seenArgs.add(args);
            return met;
        });
    }

    @Test
    void unmetRequirementsRefuseTheStart() {
        quest.setRequirements(Conditions.of(List.of(Map.of("has_permission", "quests.vip"))));

        assertEquals(QuestStartResult.CONDITIONS_NOT_MET,
                service(false).canStartQuest(actor, quest, QuestDistributionStrategy.RANDOM, profile));
        assertEquals(Map.of("quest", "mine", "quest_title", "Mine", "group", "daily"), seenArgs.get(0));
    }

    @Test
    void metRequirementsLetItStart() {
        quest.setRequirements(Conditions.of(List.of(Map.of("has_permission", "quests.vip"))));

        assertEquals(QuestStartResult.SUCCESS,
                service(true).canStartQuest(actor, quest, QuestDistributionStrategy.RANDOM, profile));
    }

    @Test
    void noRequirementsOrNoProfileSkipTheCheck() {
        assertEquals(QuestStartResult.SUCCESS,
                service(false).canStartQuest(actor, quest, QuestDistributionStrategy.RANDOM, profile));

        quest.setRequirements(Conditions.of(List.of(Map.of("has_permission", "quests.vip"))));
        assertEquals(QuestStartResult.SUCCESS,
                service(false).canStartQuest(actor, quest, QuestDistributionStrategy.RANDOM, null));
        assertTrue(seenArgs.isEmpty());
    }
}
