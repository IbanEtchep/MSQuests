package com.github.ibanetchep.msquests.database;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;
import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.ArtisanQuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.PlayerProfileSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.RotationSqlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** H2 only (no Docker needed): the copy of msquests_* into artisan_quests_* and reading it back. */
class LegacyDataCopierTest {

    @TempDir
    File dataFolder;

    private DbAccess db;
    private final UUID actor = UUID.randomUUID();
    private final UUID active = UUID.randomUUID();
    private final UUID done = UUID.randomUUID();
    private final UUID orphan = UUID.randomUUID();

    private static final MigrationMap MAP = new MigrationMap(Map.of(
            "daily", new MigrationMap.Group("G1", Map.of(
                    "mine", new MigrationMap.Quest("Q1", Map.of("stone", "s1", "dirt", "s2")),
                    "fish", new MigrationMap.Quest("Q2", Map.of("cod", "s1"))))));

    @BeforeEach
    void setUp() throws Exception {
        db = new DbAccess();
        db.initPool(new DbCredentials("h2", null, null, null, null, 0, dataFolder));

        new ActorSqlRepository(db).add(new QuestActorDTO("player", actor)).get();
        new PlayerProfileSqlRepository(db).save(new PlayerProfileDTO(actor, "Steve", active)).get();
        QuestSqlRepository legacy = new QuestSqlRepository(db);
        legacy.save(quest(active, "mine", QuestStatus.IN_PROGRESS, Map.of(
                "stone", objective(active, "stone", QuestObjectiveStatus.COMPLETED, 64),
                "dirt", objective(active, "dirt", QuestObjectiveStatus.IN_PROGRESS, 3),
                "removed", objective(active, "removed", QuestObjectiveStatus.IN_PROGRESS, 9)))).get();
        legacy.save(quest(done, "fish", QuestStatus.COMPLETED, Map.of(
                "cod", objective(done, "cod", QuestObjectiveStatus.COMPLETED, 5)))).get();
        legacy.save(quest(orphan, "deleted_quest", QuestStatus.IN_PROGRESS, Map.of(
                "x", objective(orphan, "x", QuestObjectiveStatus.IN_PROGRESS, 1)))).get();
        RotationSqlRepository rotations = new RotationSqlRepository(db);
        rotations.save(actor, "daily").get();
        rotations.save(actor, "gone").get();
    }

    @AfterEach
    void tearDown() {
        db.closePool();
    }

    @Test
    void planCountsWithoutWriting() {
        LegacyDataCopier copier = new LegacyDataCopier(db.getJdbi());

        LegacyDataCopier.Result plan = copier.plan(MAP);

        assertEquals("msquests_player_profile: 1/1 copied; msquests_actor: 1/1 copied;"
                + " msquests_quest: 2/3 copied, 1 left behind; msquests_objective: 3/5 copied, 2 left behind;"
                + " msquests_rotation: 1/2 copied, 1 left behind", plan.summary());
        assertFalse(copier.isCopied());
        assertTrue(new ArtisanQuestSqlRepository(db).getAllByActor(actor).join().isEmpty());
    }

    @Test
    void copiedProgressReadsBackTheSameThroughUids() {
        Map<UUID, QuestDTO> before = new QuestSqlRepository(db).getAllByActor(actor).join();

        new LegacyDataCopier(db.getJdbi()).copy(MAP);
        Map<UUID, QuestDTO> after = new ArtisanQuestSqlRepository(db).getAllByActor(actor).join();

        assertEquals(Map.of(active, "Q1", done, "Q2"), Map.of(
                active, after.get(active).questUid(), done, after.get(done).questUid()));
        for (UUID id : after.keySet()) {
            QuestDTO old = before.get(id);
            QuestDTO now = after.get(id);
            assertEquals(old.status(), now.status());
            assertEquals(old.questKey(), now.questKey());
            assertEquals(old.groupKey(), now.groupKey());
            assertEquals(old.createdAt(), now.createdAt(), "period membership depends on created_at");
            assertEquals(old.completedAt(), now.completedAt());
            for (QuestObjectiveDTO objective : now.objectives().values()) {
                QuestObjectiveDTO was = old.objectives().get(objective.objectiveKey());
                assertEquals(was.progress(), objective.progress());
                assertEquals(was.objectiveStatus(), objective.objectiveStatus());
            }
        }
        assertEquals("s2", after.get(active).objectives().get("dirt").stageKey());
        assertFalse(after.get(active).objectives().containsKey("removed"), "objective no longer in its quest");
        assertFalse(after.containsKey(orphan), "quest no longer in the configs");

        assertEquals(1, new RotationSqlRepository(db, SqlTables.ARTISAN)
                .countInPeriod(actor, "G1", null, null).join());
        assertEquals(active, new PlayerProfileSqlRepository(db, SqlTables.ARTISAN).get(actor).join().trackedQuestId());
        assertNotNull(new ActorSqlRepository(db, SqlTables.ARTISAN).get(actor).join());

        // The old tables are left as they were, for the previous jar.
        assertEquals(before.keySet(), new QuestSqlRepository(db).getAllByActor(actor).join().keySet());
    }

    @Test
    void copyRunsOnce() {
        LegacyDataCopier copier = new LegacyDataCopier(db.getJdbi());
        copier.copy(MAP);

        LegacyDataCopier.Result second = copier.copy(MAP);

        assertTrue(second.alreadyCopied());
        assertTrue(copier.isCopied());
        assertEquals(2, new ArtisanQuestSqlRepository(db).getAllByActor(actor).join().size());
    }

    @Test
    void refusesToMixWithRowsItDidNotWrite() {
        new ActorSqlRepository(db, SqlTables.ARTISAN).add(new QuestActorDTO("player", UUID.randomUUID())).join();
        LegacyDataCopier copier = new LegacyDataCopier(db.getJdbi());

        assertThrows(IllegalStateException.class, () -> copier.copy(MAP));
        assertFalse(copier.isCopied());
    }

    @Test
    void artisanSaveFollowsAnObjectiveMovedToAnotherStage() {
        new LegacyDataCopier(db.getJdbi()).copy(MAP);
        ArtisanQuestSqlRepository repository = new ArtisanQuestSqlRepository(db);
        QuestDTO loaded = repository.getAllByActor(actor).join().get(active);

        repository.save(new QuestDTO(loaded.id(), "mine_renamed", loaded.groupKey(), actor, loaded.status(), null,
                loaded.createdAt(), loaded.updatedAt(), Map.of("dirt", new QuestObjectiveDTO(active, "dirt",
                "block_break", QuestObjectiveStatus.IN_PROGRESS, 4, "s3")), "Q1")).join();

        QuestDTO reloaded = repository.getAllByActor(actor).join().get(active);
        assertEquals("mine_renamed", reloaded.questKey(), "last-known key refreshed");
        assertEquals("s3", reloaded.objectives().get("dirt").stageKey());
        assertEquals(4, reloaded.objectives().get("dirt").progress());
        assertEquals(2, reloaded.objectives().size(), "one row per objective, no duplicate left in s2");
    }

    private QuestDTO quest(UUID id, String key, QuestStatus status, Map<String, QuestObjectiveDTO> objectives) {
        long created = Instant.parse("2026-10-16T08:00:00Z").toEpochMilli();
        return new QuestDTO(id, key, "daily", actor, status,
                status == QuestStatus.COMPLETED ? created + 60_000 : null, created, created, objectives);
    }

    private static QuestObjectiveDTO objective(UUID quest, String key, QuestObjectiveStatus status, int progress) {
        return new QuestObjectiveDTO(quest, key, "block_break", status, progress);
    }
}
