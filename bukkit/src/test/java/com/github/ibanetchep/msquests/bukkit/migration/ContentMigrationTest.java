package com.github.ibanetchep.msquests.bukkit.migration;

import com.github.ibanetchep.msquests.bukkit.config.ContentSource;
import com.github.ibanetchep.msquests.bukkit.document.MigrationMapFile;
import com.github.ibanetchep.msquests.bukkit.document.QuestGroupDocumentRepository;
import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.DbCredentials;
import com.github.ibanetchep.msquests.database.LegacyDataCopier;
import com.github.ibanetchep.msquests.database.repository.ActorSqlRepository;
import com.github.ibanetchep.msquests.database.repository.ArtisanQuestSqlRepository;
import com.github.ibanetchep.msquests.database.repository.QuestSqlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The whole switch on the production daily group: dry run, confirm, the copy made at the next
 * start, then the quests and progress read the artisan way.
 */
class ContentMigrationTest {

    @TempDir
    Path dataFolder;

    /** Artisan's {@code projects/quests}, beside MSQuests' data folder on a server. */
    private Path project() {
        return dataFolder.resolve("artisan/projects/quests");
    }

    private DbAccess db;
    private final UUID player = UUID.randomUUID();
    private final UUID stoneQuest = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(dataFolder.resolve("quests"));
        Files.copy(Path.of("../docker/plugins/MSQuests/quests/daily.yml"), dataFolder.resolve("quests/daily.yml"));

        db = new DbAccess();
        db.initPool(new DbCredentials("h2", null, null, null, null, 0, dataFolder.toFile()));
        new ActorSqlRepository(db).add(new QuestActorDTO("player", player)).join();
        long now = System.currentTimeMillis();
        new QuestSqlRepository(db).save(new QuestDTO(stoneQuest, "mine_stone_64", "daily", player,
                QuestStatus.IN_PROGRESS, null, now, now, Map.of("obj_1", new QuestObjectiveDTO(
                stoneQuest, "obj_1", "block_break", QuestObjectiveStatus.IN_PROGRESS, 37)))).join();
    }

    @AfterEach
    void tearDown() {
        db.closePool();
    }

    private ContentMigration migration() {
        return new ContentMigration(dataFolder, project(), "fr_FR", ContentSource.LEGACY,
                new LegacyDataCopier(db.getJdbi()), Logger.getAnonymousLogger());
    }

    @Test
    void dryRunWritesOnlyTheReport() throws Exception {
        ContentMigration.Outcome outcome = migration().run(false);

        assertFalse(outcome.confirmed());
        assertEquals(1, outcome.groups());
        assertEquals(1, outcome.documentsToWrite());
        assertEquals(0, outcome.warnings());
        assertTrue(Files.exists(outcome.report()));
        String report = Files.readString(outcome.report());
        assertTrue(report.contains("Read back: same content as quests/daily.yml."), report);
        assertTrue(report.contains("msquests_quest: 1/1 copied"), report);
        assertFalse(Files.exists(project().resolve("quest_groups/daily.yaml")));
        assertFalse(Files.exists(ContentMigration.mapFile(dataFolder)));
        assertFalse(new LegacyDataCopier(db.getJdbi()).isCopied());
    }

    @Test
    void confirmedMigrationReloadsTheSameQuestsWithTheSameProgress() throws Exception {
        QuestGroupConfigDTO legacyDaily = new com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository(
                dataFolder.resolve("quests"), Logger.getAnonymousLogger()).loadAllGroups().join().groups().get("daily");

        migration().run(true);
        // What the next start with content_source: artisan does.
        MigrationMap map = MigrationMapFile.read(ContentMigration.mapFile(dataFolder));
        new LegacyDataCopier(db.getJdbi()).copy(map);

        var documents = new QuestGroupDocumentRepository(project()).loadAllGroups().join();
        assertFalse(documents.hasFailures(), documents.failures().toString());
        QuestGroupConfigDTO daily = documents.groups().get("daily");
        assertEquals(legacyDaily.quests().size(), daily.quests().size());
        assertTrue(Files.readString(project().resolve("manifest.yaml")).contains("default_lang: fr"));

        QuestDTO stored = new ArtisanQuestSqlRepository(db).getAllByActor(player).join().get(stoneQuest);
        QuestConfigDTO config = daily.quests().stream().filter(q -> q.uid().equals(stored.questUid())).findFirst().orElseThrow();
        assertEquals("mine_stone_64", config.key());
        assertEquals(37, stored.objectives().get("obj_1").progress());
        assertEquals("stage_1", stored.objectives().get("obj_1").stageKey());

        // Untouched for the previous jar.
        assertTrue(Files.exists(dataFolder.resolve("quests/daily.yml")));
        assertEquals(37, new QuestSqlRepository(db).getAllByActor(player).join().get(stoneQuest)
                .objectives().get("obj_1").progress());

        // A second confirmed run changes nothing and reports the copy as done.
        String before = Files.readString(project().resolve("quest_groups/daily.yaml"));
        ContentMigration.Outcome again = migration().run(true);
        assertEquals(0, again.documentsToWrite());
        assertTrue(again.database().alreadyCopied());
        assertEquals(before, Files.readString(project().resolve("quest_groups/daily.yaml")));
        assertEquals(map, MigrationMapFile.read(ContentMigration.mapFile(dataFolder)));
    }
}
