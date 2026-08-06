package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestAction;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TRANSLATOR;
import static org.junit.jupiter.api.Assertions.*;

class QuestRowsTest {

    private QuestGroupConfig group;
    private QuestConfig mineStone;
    private QuestConfig killZombies;
    private TestActor actor;

    @BeforeEach
    void setUp() {
        group = ArtisanFixtures.group("daily", "PLAYER");
        mineStone = ArtisanFixtures.quest(group, "mine_stone", "easy");
        killZombies = ArtisanFixtures.quest(group, "kill_zombies", "hard");
        actor = new TestActor(UUID.randomUUID(), "Steve", "player");
    }

    private ActorQuestGroup actorGroup() {
        return actor.getActorQuestGroup(group);
    }

    // ---------------------------------------------------------------- quests

    @Test
    void questRowKeyIsGroupScopedSoItIsUniqueAcrossGroups() {
        Map<String, Object> row = QuestRows.questRow(group, mineStone, null, TRANSLATOR);

        assertEquals("daily:mine_stone", row.get("id"));
        assertEquals("daily", row.get("group"));
        assertEquals("mine_stone", row.get("key"));
    }

    @Test
    void questRowWithoutAnActorIsNotStartedAndNeutral() {
        Map<String, Object> row = QuestRows.questRow(group, mineStone, null, TRANSLATOR);

        assertEquals("NOT_STARTED", row.get("status_key"));
        assertEquals(0, row.get("objective_progress"));
        assertEquals(0, row.get("objective_target"));
        assertEquals(0.0, (Double) row.get("progress_ratio"));
        assertEquals("", row.get("objective_name"));
    }

    // -------------------------------------------------- status vs startability

    /**
     * The two axes must not be conflated: {@code status_key} describes the player's
     * current attempt, {@code can_start} whether a new one may begin. A rankup quest
     * is NOT_STARTED and un-startable at the same time.
     */
    @Test
    void statusKeySpeaksTheDomainVocabulary() {
        assertEquals("NOT_STARTED", QuestRows.questRow(group, mineStone, null, TRANSLATOR).get("status_key"));

        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 10);
        assertEquals("IN_PROGRESS",
                QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR).get("status_key"));

        ArtisanFixtures.instance(actor, killZombies, QuestStatus.COMPLETED, 64);
        assertEquals("COMPLETED",
                QuestRows.questRow(group, killZombies, actorGroup(), TRANSLATOR).get("status_key"));
    }

    /** An expired attempt is over: the quest is offerable again, not stuck on EXPIRED. */
    @Test
    void anExpiredAttemptReadsAsNotStarted() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.EXPIRED, 12);

        assertEquals("NOT_STARTED",
                QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR).get("status_key"));
    }

    /** What a menu filters on to show "my quests" instead of the whole catalogue. */
    @Test
    void assignedSaysWhetherTheActorGotThisQuestThisPeriod() {
        assertEquals(false, QuestRows.questRow(group, mineStone, null, TRANSLATOR).get("assigned"));

        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 10);
        assertEquals(true, QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR).get("assigned"));
        // Never handed out, even though the actor has other quests of the group.
        assertEquals(false, QuestRows.questRow(group, killZombies, actorGroup(), TRANSLATOR).get("assigned"));
    }

    @Test
    void aFailedAttemptIsReportedAsSuch() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.FAILED, 12);

        assertEquals("FAILED",
                QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR).get("status_key"));
    }

    @Test
    void aStartableQuestSaysSoWithNoBlocker() {
        Map<String, Object> row = QuestRows.questRow(
                group, mineStone, null, qc -> QuestStartResult.SUCCESS, TRANSLATOR);

        assertEquals(true, row.get("can_start"));
        assertEquals("", row.get("start_blocker"));
        assertEquals("", row.get("start_blocker_label"));
    }

    /** The rankup case: one quest at a time, so the next one is locked, with a reason. */
    @Test
    void aLockedQuestCarriesTheReasonItCannotStart() {
        Map<String, Object> row = QuestRows.questRow(
                group, killZombies, null, qc -> QuestStartResult.MAX_ACTIVE_REACHED, TRANSLATOR);

        assertEquals("NOT_STARTED", row.get("status_key"));
        assertEquals(false, row.get("can_start"));
        assertEquals("MAX_ACTIVE_REACHED", row.get("start_blocker"));
        assertEquals("quest.start.result.max_active_reached", row.get("start_blocker_label"));
    }

    @Test
    void everyBlockerReasonSurvivesToTheRow() {
        for (QuestStartResult result : QuestStartResult.values()) {
            Map<String, Object> row =
                    QuestRows.questRow(group, mineStone, null, qc -> result, TRANSLATOR);

            assertEquals(result.isSuccess(), row.get("can_start"), result.name());
            assertEquals(result.isSuccess() ? "" : result.name(), row.get("start_blocker"), result.name());
        }
    }

    /** No actor (editor preview): startability is unknowable, so it is not claimed. */
    @Test
    void withoutAStartCheckTheQuestIsNotClaimedStartable() {
        Map<String, Object> row = QuestRows.questRow(group, mineStone, null, TRANSLATOR);

        assertEquals(false, row.get("can_start"));
        assertEquals("", row.get("start_blocker"));
    }

    @Test
    void questRowResolvesTierNameFromTheGroup() {
        Map<String, Object> row = QuestRows.questRow(group, mineStone, null, TRANSLATOR);

        assertEquals("easy", row.get("tier"));
        assertEquals("Facile", row.get("tier_name"));
    }

    @Test
    void unknownTierLeavesTheNameEmptyRatherThanFailing() {
        Map<String, Object> row = QuestRows.questRow(group, killZombies, null, TRANSLATOR);

        assertEquals("hard", row.get("tier"));
        assertEquals("", row.get("tier_name"));
    }

    @Test
    void activeQuestReportsProgressReadyToDisplay() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 34);

        Map<String, Object> row = QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR);

        assertEquals("IN_PROGRESS", row.get("status_key"));
        assertEquals(34, row.get("objective_progress"));
        assertEquals(64, row.get("objective_target"));
        assertEquals(34.0 / 64.0, (Double) row.get("progress_ratio"), 0.0001);
        assertEquals("objective.block_break", row.get("objective_name"));
    }

    @Test
    void completedQuestIsReportedAsCompletedAtFullRatio() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.COMPLETED, 64);

        Map<String, Object> row = QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR);

        assertEquals("COMPLETED", row.get("status_key"));
        assertEquals(1.0, (Double) row.get("progress_ratio"));
    }



    // ------------------------------------------------------- catalog: le tree

    @Test
    @SuppressWarnings("unchecked")
    void questRowDescribesItsStagesAndObjectivesWithoutAnyInstance() {
        Map<String, Object> row = QuestRows.questRow(group, mineStone, null, TRANSLATOR);

        List<Map<String, Object>> stages = (List<Map<String, Object>>) row.get("stages");
        assertEquals(1, stages.size());
        Map<String, Object> stage = stages.get(0);
        assertEquals("stage_1", stage.get("key"));
        assertEquals("Stage 1", stage.get("name"));
        assertEquals("PARALLEL", stage.get("flow"));
        assertEquals(0, stage.get("index"));
        assertEquals(false, stage.get("completed"));
        assertEquals(0.0, (Double) stage.get("progress_ratio"));

        List<Map<String, Object>> objectives = (List<Map<String, Object>>) stage.get("objectives");
        assertEquals(1, objectives.size());
        Map<String, Object> objective = objectives.get(0);
        assertEquals("obj_1", objective.get("key"));
        assertEquals("block_break", objective.get("type"));
        assertEquals("objective.block_break", objective.get("name"));
        // The target is configuration, so it is known before the player starts anything.
        assertEquals(64, objective.get("target"));
        assertEquals(0, objective.get("progress"));
        assertEquals(false, objective.get("completed"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void questRowOverlaysTheRunningInstanceOntoTheConfiguredTree() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 34);

        Map<String, Object> row = QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR);

        Map<String, Object> stage = ((List<Map<String, Object>>) row.get("stages")).get(0);
        assertEquals(34.0 / 64.0, (Double) stage.get("progress_ratio"), 0.0001);
        Map<String, Object> objective = ((List<Map<String, Object>>) stage.get("objectives")).get(0);
        assertEquals(34, objective.get("progress"));
        assertEquals(64, objective.get("target"));
        assertEquals(false, objective.get("completed"));
    }

    /** A quest finished this period keeps showing its tree, filled in. */
    @Test
    @SuppressWarnings("unchecked")
    void questRowOverlaysACompletedInstanceToo() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.COMPLETED, 64);

        Map<String, Object> row = QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR);

        Map<String, Object> objective =
                ((List<Map<String, Object>>) ((List<Map<String, Object>>) row.get("stages")).get(0).get("objectives")).get(0);
        assertEquals(64, objective.get("progress"));
        assertEquals(true, objective.get("completed"));
    }

    /** An expired instance is not the player's current attempt — the tree resets to zero. */
    @Test
    @SuppressWarnings("unchecked")
    void questRowIgnoresAnExpiredInstance() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.EXPIRED, 12);

        Map<String, Object> row = QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR);

        Map<String, Object> objective =
                ((List<Map<String, Object>>) ((List<Map<String, Object>>) row.get("stages")).get(0).get("objectives")).get(0);
        assertEquals(0, objective.get("progress"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rewardsAreAlsoExposedAsRowsSoAMenuCanIterateThem() {
        mineStone.addReward(new TestAction("command", "<white>+150 XP"));
        mineStone.addReward(new TestAction("give_item", null));

        List<Map<String, Object>> rewards =
                (List<Map<String, Object>>) QuestRows.questRow(group, mineStone, null, TRANSLATOR).get("rewards");

        assertEquals(2, rewards.size());
        assertEquals("command", rewards.get(0).get("type"));
        assertEquals("<white>+150 XP", rewards.get(0).get("name"));
        // Unnamed reward: the type stands in, same rule as rewards_lore.
        assertEquals("give_item", rewards.get(1).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void multiStageQuestsKeepTheirStagesOrderedAndIndexed() {
        QuestConfig epic = ArtisanFixtures.multiStageQuest(group, "epic", 3);

        List<Map<String, Object>> stages =
                (List<Map<String, Object>>) QuestRows.questRow(group, epic, null, TRANSLATOR).get("stages");

        assertEquals(3, stages.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("stage_" + i, stages.get(i).get("key"));
            assertEquals(i, stages.get(i).get("index"));
        }
    }

    @Test
    void theWholeTreeStaysJsonShaped() {
        mineStone.addReward(new TestAction("command", "+150 XP"));
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 34);

        assertJsonShaped(QuestRows.questRow(group, mineStone, actorGroup(), TRANSLATOR));
    }

    // ---------------------------------------------------------------- groups

    @Test
    void groupRowCarriesConfigAndPerActorCounts() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 10);
        ArtisanFixtures.instance(actor, killZombies, QuestStatus.COMPLETED, 5);

        Map<String, Object> row = QuestRows.groupRow(group, actorGroup(), TRANSLATOR);

        assertEquals("daily", row.get("key"));
        assertEquals("PLAYER", row.get("actor_type"));
        assertEquals(6, row.get("max_active"));
        assertEquals(true, row.get("rotatable"));
        assertEquals(1, row.get("active_count"));
        assertEquals(1, row.get("completed_count"));
    }

    @Test
    void groupRowWithoutAnActorCountsZeroSoPreviewStaysUsable() {
        Map<String, Object> row = QuestRows.groupRow(group, null, TRANSLATOR);

        assertEquals(0, row.get("active_count"));
        assertEquals(0, row.get("completed_count"));
        assertEquals(false, row.get("can_rotate"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void groupRowNestsItsQuestRowsForPointedPagination() {
        Map<String, Object> row = QuestRows.groupRow(group, null, TRANSLATOR);

        List<Map<String, Object>> quests = (List<Map<String, Object>>) row.get("quests");
        assertEquals(2, quests.size());
        assertEquals("daily:mine_stone", quests.get(0).get("id"));
    }

    /** Instants travel raw; a group with no period reads 0, and the menu decides. */
    @Test
    void groupWithoutAPeriodCarriesZeroRatherThanNull() {
        Map<String, Object> row = QuestRows.groupRow(group, null, TRANSLATOR);

        assertEquals(0L, row.get("period_end"));
    }

    // --------------------------------------------------------------- actives

    @Test
    @SuppressWarnings("unchecked")
    void activeRowExposesStagesAndObjectives() {
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 34);

        Map<String, Object> row = QuestRows.activeRow(quest, quest.getId(), TRANSLATOR);

        assertEquals(quest.getId().toString(), row.get("id"));
        assertEquals("mine_stone", row.get("quest_key"));
        assertEquals("daily", row.get("group"));
        assertEquals(true, row.get("tracked"));
        assertEquals(1, row.get("stage_count"));
        assertEquals("Stage 1", row.get("stage_name"));
        assertEquals("PARALLEL", row.get("stage_flow"));

        List<Map<String, Object>> objectives = (List<Map<String, Object>>) row.get("objectives");
        assertEquals(1, objectives.size());
        Map<String, Object> objective = objectives.get(0);
        assertEquals("obj_1", objective.get("key"));
        assertEquals("block_break", objective.get("type"));
        assertEquals(34, objective.get("progress"));
        assertEquals(64, objective.get("target"));
        assertEquals(false, objective.get("completed"));
    }

    @Test
    void untrackedQuestIsReportedAsSuch() {
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 1);

        assertEquals(false, QuestRows.activeRow(quest, null, TRANSLATOR).get("tracked"));
        assertEquals(false, QuestRows.activeRow(quest, UUID.randomUUID(), TRANSLATOR).get("tracked"));
    }

    /** Rows carry the domain ratio as-is, so they must stay on its 0..1 scale. */
    @Test
    void progressRatioStaysInTheZeroOneRange() {
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 32);

        double ratio = (Double) QuestRows.activeRow(quest, null, TRANSLATOR).get("progress_ratio");
        assertEquals(0.5, ratio, 0.0001);
    }

    @Test
    void everyValueIsJsonShapedSoItSurvivesTheWebSocket() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 34);

        assertJsonShaped(QuestRows.groupRow(group, actorGroup(), TRANSLATOR));
    }

    @SuppressWarnings("unchecked")
    private void assertJsonShaped(Object value) {
        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                assertInstanceOf(String.class, k);
                assertJsonShaped(v);
            });
        } else if (value instanceof List<?> list) {
            list.forEach(this::assertJsonShaped);
        } else {
            assertTrue(value instanceof String || value instanceof Number || value instanceof Boolean,
                    "not JSON-shaped: " + value);
        }
    }
}
