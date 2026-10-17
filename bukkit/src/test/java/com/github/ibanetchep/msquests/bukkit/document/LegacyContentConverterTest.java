package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.migration.MigrationMap;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LegacyContentConverterTest {

    /** The daily group as it runs in production, plus the test group (conditions, two stages). */
    private static final Path DAILY = Path.of("../docker/plugins/MSQuests/quests/daily.yml");
    private static final Path GROUP_1 = Path.of("src/test/resources/quests/group_1.yml");

    @TempDir
    Path dir;

    private Path project;
    private QuestConfigLoad legacy;

    @BeforeEach
    void setUp() throws Exception {
        Path quests = dir.resolve("quests");
        Files.createDirectories(quests);
        Files.copy(DAILY, quests.resolve("daily.yml"));
        Files.copy(GROUP_1, quests.resolve("group_1.yml"));
        legacy = new QuestConfigYamlRepository(quests, Logger.getAnonymousLogger()).loadAllGroups().join();
        assertFalse(legacy.hasFailures(), legacy.failures().toString());

        project = dir.resolve("editor");
        Files.createDirectories(project.resolve("quest_groups"));
        Files.writeString(project.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: fr\nlanguages: [fr]\n");
    }

    private LegacyContentConverter converter() {
        return new LegacyContentConverter("fr", "Europe/Paris");
    }

    @Test
    void convertedDocumentsLoadBackAsTheSameGroups() throws Exception {
        var plan = converter().plan(legacy.groups(), legacy.failures(), project.resolve("quest_groups"), MigrationMap.empty());
        assertEquals(0, plan.report().warnings(), plan.report().render());
        assertEquals(2, plan.toWrite());

        converter().apply(plan, project.resolve("quest_groups"), dir.resolve("tmp"));
        QuestConfigLoad docs = new QuestGroupDocumentRepository(project).loadNow();
        assertFalse(docs.hasFailures(), docs.failures().toString());
        assertEquals(legacy.groups().keySet(), docs.groups().keySet());

        for (QuestGroupConfigDTO original : legacy.groups().values()) {
            QuestGroupConfigDTO read = docs.groups().get(original.key());
            assertEquals(java.util.List.of(), LegacyContentConverter.differences(original, read), original.key());
            assertEquals(plan.map().group(original.key()).uid(), read.uid());
            assertEquals("Europe/Paris", read.resetTimezone());
            for (QuestConfigDTO quest : read.quests()) {
                assertEquals(plan.map().quest(original.key(), quest.key()).uid(), quest.uid());
            }
        }
        assertEquals(original("daily").quests().size(), docs.groups().get("daily").quests().size());
    }

    @Test
    void documentUsesTheEditorFieldNames() throws Exception {
        var plan = converter().plan(legacy.groups(), legacy.failures(), project.resolve("quest_groups"), MigrationMap.empty());
        String yaml = plan.groups().stream().filter(g -> g.key().equals("daily")).findFirst().orElseThrow().yaml();

        assertTrue(yaml.startsWith("schema: quest_group/v1\n"), yaml.substring(0, 80));
        assertTrue(yaml.contains("title:\n  fr: Quêtes journalières"), "group title as ltext");
        assertTrue(yaml.contains("reset:\n  cron: '*/2 * * * *'\n  timezone: Europe/Paris"), "cron with its zone");
        assertTrue(yaml.contains("duration: 24h"), "duration in hours");
        assertTrue(yaml.contains("strategy: random"));
        assertTrue(yaml.contains("period_complete:"), "all_period_quests_complete hook renamed");
        assertFalse(yaml.contains("\nkey: daily"), "the group key is the Document id, not a field");
        assertTrue(yaml.contains("    \"on\":\n      complete:\n") || yaml.contains("    on:\n      complete:\n"), "quest actions at completion under on.complete");
        assertFalse(yaml.contains("rewards:"), "quest actions at completion live under on.complete");
        assertFalse(yaml.contains("reward_display"), "reward lines are part of the description");
    }

    @Test
    void rerunKeepsUidsAndNeverOverwrites() throws Exception {
        Path groupsDir = project.resolve("quest_groups");
        var first = converter().plan(legacy.groups(), legacy.failures(), groupsDir, MigrationMap.empty());
        converter().apply(first, groupsDir, dir.resolve("tmp"));
        Path mapFile = dir.resolve("migration/map.yaml");
        MigrationMapFile.write(mapFile, first.map(), dir.resolve("tmp"));
        Path daily = groupsDir.resolve("daily.yaml");
        Files.writeString(daily, Files.readString(daily).replace("fr: Apprenti carrier", "fr: Edited in the editor"));

        var second = converter().plan(legacy.groups(), legacy.failures(), groupsDir, MigrationMapFile.read(mapFile));

        assertEquals(0, second.toWrite());
        assertEquals(first.map(), second.map());
        assertTrue(Files.readString(daily, StandardCharsets.UTF_8).contains("Edited in the editor"));
    }

    @Test
    void rerunFindsADocumentRenamedInTheEditor() throws Exception {
        Path groupsDir = project.resolve("quest_groups");
        var first = converter().plan(legacy.groups(), legacy.failures(), groupsDir, MigrationMap.empty());
        converter().apply(first, groupsDir, dir.resolve("tmp"));
        Files.move(groupsDir.resolve("daily.yaml"), groupsDir.resolve("quotidiennes.yaml"));

        var second = converter().plan(legacy.groups(), legacy.failures(), groupsDir, first.map());
        converter().apply(second, groupsDir, dir.resolve("tmp"));

        assertEquals(0, second.toWrite());
        assertEquals(first.map(), second.map());
        assertFalse(Files.exists(groupsDir.resolve("daily.yaml")));
        QuestConfigLoad load = new QuestGroupDocumentRepository(project).loadNow();
        assertFalse(load.hasFailures(), load.failures().toString());
    }

    @Test
    void sharedObjectiveKeyIsRenamedAndReported() throws Exception {
        Path quests = dir.resolve("dup");
        Files.createDirectories(quests);
        Files.writeString(quests.resolve("dup.yml"), """
                key: dup
                name: Dup
                description: d
                max_active: 1
                actor_type: PLAYER
                actions: { quest_start: [], quest_complete: [], objective_progress: [], objective_complete: [] }
                quests:
                  - key: q
                    name: Q
                    description: d
                    stages:
                      - key: s1
                        flow: PARALLEL
                        objectives: [ { key: obj, type: block_break, material: STONE, amount: 1 } ]
                      - key: s2
                        flow: PARALLEL
                        objectives: [ { key: obj, type: block_break, material: DIRT, amount: 1 } ]
                """);
        QuestConfigLoad load = new QuestConfigYamlRepository(quests, Logger.getAnonymousLogger()).loadAllGroups().join();

        var plan = converter().plan(load.groups(), load.failures(), project.resolve("quest_groups"), MigrationMap.empty());

        assertTrue(plan.report().render().contains("becomes 'obj_s2'"), plan.report().render());
        assertEquals(java.util.Map.of("obj", "s1"), plan.map().quest("dup", "q").objectiveStages());
        assertTrue(plan.groups().get(0).yaml().contains("key: obj_s2"));
    }

    private QuestGroupConfigDTO original(String key) {
        return legacy.groups().get(key);
    }
}
