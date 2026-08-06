package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * "The actor's current attempt at a quest" is the question every display surface asks
 * (menus, placeholders, the Artisan catalog). It lives here so they all get the same answer.
 */
class ActorQuestGroupTest {

    private QuestGroupConfig group;
    private QuestConfig questConfig;
    private TestActor actor;

    @BeforeEach
    void setUp() {
        group = new QuestGroupConfig.Builder("daily", "Daily", "desc", "PLAYER").maxActive(6).build();
        questConfig = new QuestConfig("mine_stone", "Mine", "desc", 0L);
        questConfig.setGroup(group);
        QuestStageConfig stage = new QuestStageConfig("stage_1", "Stage", Flow.PARALLEL);
        stage.addObjective(new TestObjectiveConfig("obj_1"));
        questConfig.addStage(stage);
        group.addQuest(questConfig);
        actor = new TestActor();
    }

    private ActorQuestGroup actorGroup() {
        return actor.getActorQuestGroup(group);
    }

    @Test
    void noInstanceMeansNoAttempt() {
        assertNull(actorGroup().getCurrentAttempt("mine_stone"));
    }

    @Test
    void unknownQuestKeyMeansNoAttempt() {
        instance(QuestStatus.IN_PROGRESS, new Date());

        assertNull(actorGroup().getCurrentAttempt("nope"));
    }

    @Test
    void theRunningInstanceIsTheCurrentAttempt() {
        Quest quest = instance(QuestStatus.IN_PROGRESS, new Date());

        assertEquals(quest, actorGroup().getCurrentAttempt("mine_stone"));
    }

    @Test
    void aFinishedInstanceOfThisPeriodIsStillTheCurrentAttempt() {
        Quest quest = instance(QuestStatus.COMPLETED, new Date());

        assertEquals(quest, actorGroup().getCurrentAttempt("mine_stone"));
    }

    /** An expired attempt is over — the quest is offerable again. */
    @Test
    void anExpiredInstanceIsNotAnAttempt() {
        instance(QuestStatus.EXPIRED, new Date());

        assertNull(actorGroup().getCurrentAttempt("mine_stone"));
    }

    @Test
    void aRunningInstanceWinsOverAFinishedOne() {
        instance(QuestStatus.COMPLETED, Date.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        Quest running = instance(QuestStatus.IN_PROGRESS, new Date());

        assertEquals(running, actorGroup().getCurrentAttempt("mine_stone"));
    }

    @Test
    void theMostRecentFinishedInstanceWins() {
        instance(QuestStatus.COMPLETED, Date.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        Quest latest = instance(QuestStatus.FAILED, new Date());

        assertEquals(latest, actorGroup().getCurrentAttempt("mine_stone"));
    }

    /** Same period rule as {@link ActorQuestGroup#hasStartedInCurrentPeriod}. */
    @Test
    void anInstanceFromBeforeThePeriodDoesNotCount() {
        QuestGroupConfig bounded = new QuestGroupConfig.Builder("bounded", "B", "d", "PLAYER")
                .startAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .endAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        QuestConfig config = new QuestConfig("old", "Old", "desc", 0L);
        config.setGroup(bounded);
        bounded.addQuest(config);

        Date longAgo = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
        Quest quest = new Quest(UUID.randomUUID(), config, actor, QuestStatus.COMPLETED, null, longAgo, longAgo);
        actor.addQuest(quest);

        ActorQuestGroup boundedGroup = actor.getActorQuestGroup(bounded);
        assertEquals(false, boundedGroup.hasStartedInCurrentPeriod("old"));
        assertNull(boundedGroup.getCurrentAttempt("old"));
    }

    /** The two answers cannot disagree: both mean "there is a live attempt this period". */
    @Test
    void currentAttemptAgreesWithHasStartedInCurrentPeriod() {
        for (QuestStatus status : QuestStatus.values()) {
            setUp();
            instance(status, new Date());
            ActorQuestGroup g = actorGroup();

            assertEquals(g.hasStartedInCurrentPeriod("mine_stone"),
                    g.getCurrentAttempt("mine_stone") != null,
                    status.name());
        }
    }

    // ----------------------------------------------------------------- helpers

    private Quest instance(QuestStatus status, Date createdAt) {
        Quest quest = new Quest(UUID.randomUUID(), questConfig, actor, status, null, createdAt, createdAt);
        QuestStage stage = new QuestStage(quest, questConfig.getStages().get("stage_1"));
        stage.addObjective(new TestObjective(stage, "obj_1"));
        quest.addStage(stage);
        actor.addQuest(quest);
        return quest;
    }

    private static final class TestObjectiveConfig extends QuestObjectiveConfig {
        TestObjectiveConfig(String key) {
            super(new QuestObjectiveConfigDTO(key, "test", Map.of()));
        }

        @Override
        public int getTarget() {
            return 1;
        }

        @Override
        public QuestObjectiveConfigDTO toDTO() {
            return new QuestObjectiveConfigDTO(key, type, Map.of());
        }

        @Override
        public Map<String, String> getPlaceholders(Translator translator) {
            return Map.of();
        }
    }

    private static final class TestObjective extends AbstractQuestObjective<TestObjectiveConfig> {
        TestObjective(QuestStage stage, String key) {
            super(stage, new TestObjectiveConfig(key), 0, QuestObjectiveStatus.IN_PROGRESS);
        }
    }

    private static final class TestActor extends QuestActor {
        TestActor() {
            super(UUID.randomUUID(), "TestActor");
        }

        @Override
        public String getActorType() {
            return "player";
        }

        @Override
        public boolean isMember(UUID playerId) {
            return id.equals(playerId);
        }
    }
}
