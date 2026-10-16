package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.bukkit.repository.QuestConfigYamlRepository;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.repository.QuestConfigLoad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class QuestGroupDocumentRepositoryTest {

    @TempDir
    Path project;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(project.resolve("quest_groups"));
        Files.writeString(project.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: en\n");
    }

    private static String group(String uid, String questUid, String extra) {
        return """
                schema: quest_group/v1
                uid: %s
                title: { fr: "Quotidiennes", en: "Daily" }
                actor_type: player
                reset: { cron: "0 4 * * *", timezone: Europe/Paris }
                limits: { max_active: 2 }
                on:
                  quest_start: [ { type: message, message: "<green>Go" } ]
                quests:
                  - key: mine
                    uid: %s
                    title: "Mine"
                    duration: 1h30m
                    stages:
                      - key: s1
                        flow: sequential
                        objectives:
                          - { key: stone, type: block_break, material: STONE, amount: 64 }
                %s""".formatted(uid, questUid, extra);
    }

    private QuestConfigLoad load() {
        return new QuestGroupDocumentRepository(project).loadNow();
    }

    @Test
    void readsTheGroupKeyFromTheFileNameAndTextsInTheDefaultLanguage() throws Exception {
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", ""));

        QuestGroupConfigDTO daily = load().groups().get("daily");

        assertEquals("daily", daily.key());
        assertEquals("Daily", daily.name());
        assertEquals("G1", daily.uid());
        assertEquals("Europe/Paris", daily.resetTimezone());
        assertEquals("message", daily.actions().questStart().getFirst().type(), "unquoted on: is a key, not YAML 1.1 true");
        assertEquals(java.util.List.of(), daily.actions().questComplete());
        QuestConfigDTO mine = daily.quests().getFirst();
        assertEquals("Q1", mine.uid());
        assertEquals(5400L, mine.duration());
        assertEquals("SEQUENTIAL", mine.stages().getFirst().flow().name());
        assertEquals(64, mine.stages().getFirst().objectives().getFirst().params().get("amount"));
    }

    @Test
    void refusesAUidUsedByTwoGroupsButKeepsTheFirst() throws Exception {
        Files.writeString(project.resolve("quest_groups/a.yaml"), group("G1", "Q1", ""));
        Files.writeString(project.resolve("quest_groups/b.yaml"), group("G2", "Q1", ""));

        QuestConfigLoad load = load();

        assertEquals(java.util.Set.of("a"), load.groups().keySet());
        assertEquals(1, load.failures().size());
        assertTrue(load.failures().getFirst().contains("uid Q1 of b/mine is already used by a/mine"), load.failures().toString());
    }

    @Test
    void aRejectedDocumentReservesNoneOfItsUids() throws Exception {
        String clashingQuest = """
                  - key: other
                    uid: Q1
                    stages:
                      - key: s1
                        objectives:
                          - { key: dirt, type: block_break, material: DIRT, amount: 1 }
                """;
        Files.writeString(project.resolve("quest_groups/a.yaml"), group("G1", "Q1", ""));
        Files.writeString(project.resolve("quest_groups/b.yaml"), group("G2", "QB", clashingQuest));
        Files.writeString(project.resolve("quest_groups/c.yaml"), group("G2", "QB", ""));

        QuestConfigLoad load = load();

        assertEquals(java.util.Set.of("a", "c"), load.groups().keySet(), load.failures().toString());
        assertEquals(1, load.failures().size());
    }

    @Test
    void reportsThePathOfAFaultyValue() throws Exception {
        Files.writeString(project.resolve("quest_groups/a.yaml"), group("G1", "Q1", "")
                .replace("duration: 1h30m", "duration: soon"));
        Files.writeString(project.resolve("quest_groups/b.yaml"), group("G2", "Q2", "")
                .replace("    uid: Q2\n", ""));
        Files.writeString(project.resolve("quest_groups/c.yaml"), group("G3", "Q3", """
                          - { key: stone, type: block_break, material: DIRT, amount: 1 }
                """));

        QuestConfigLoad load = load();

        assertTrue(load.groups().isEmpty());
        String failures = String.join("\n", load.failures());
        assertTrue(failures.contains("quest_groups/a.yaml: quests[0].duration: expected a duration"), failures);
        assertTrue(failures.contains("quest_groups/b.yaml: quests[0].uid: is required"), failures);
        assertTrue(failures.contains("quest_groups/c.yaml: quests[0].stages[0].objectives[1].key: objective key 'stone'"), failures);
    }

    @Test
    void fingerprintChangesOnlyWithTheDocuments() throws Exception {
        QuestGroupDocumentRepository repository = new QuestGroupDocumentRepository(project);
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", ""));
        String before = repository.fingerprint();

        assertEquals(before, repository.fingerprint());
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", "").replace("64", "65"));
        assertNotEquals(before, repository.fingerprint());
    }

    @Test
    void aLegacyFileCannotCarryAUid() throws Exception {
        Path quests = project.resolve("legacy");
        Files.createDirectories(quests);
        Files.writeString(quests.resolve("g.yml"), """
                key: g
                uid: SMUGGLED
                name: G
                actor_type: PLAYER
                quests:
                  - { key: q, uid: ALSO, stages: [] }
                """);

        QuestGroupConfigDTO group = new QuestConfigYamlRepository(quests, Logger.getAnonymousLogger())
                .loadAllGroups().join().groups().get("g");

        assertNull(group.uid());
        assertNull(group.quests().getFirst().uid());
    }
}
