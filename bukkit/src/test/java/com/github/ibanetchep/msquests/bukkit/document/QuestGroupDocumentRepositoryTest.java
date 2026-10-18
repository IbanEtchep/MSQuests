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
                  quest_start: [ { send_message: "<green>Go" } ]
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
        assertEquals(java.util.Map.of("send_message", "<green>Go"), daily.actions().questStart().getFirst(),
                "unquoted on: is a key, not YAML 1.1 true");
        assertEquals(java.util.List.of(), daily.actions().questComplete());
        QuestConfigDTO mine = daily.quests().getFirst();
        assertEquals("Q1", mine.uid());
        assertEquals(5400L, mine.duration());
        assertEquals("SEQUENTIAL", mine.stages().getFirst().flow().name());
        assertEquals(64, mine.stages().getFirst().objectives().getFirst().params().get("amount"));
    }

    @Test
    void questActionsAtCompletionAreReadFromActionsComplete() throws Exception {
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", """
                    actions:
                      complete: [ { send_message: "<green>Bravo" } ]
                """));

        QuestConfigDTO mine = load().groups().get("daily").quests().getFirst();

        assertEquals(java.util.List.of(java.util.Map.of("send_message", "<green>Bravo")), mine.rewards());
    }

    @Test
    void questHooksAreReadFromActions() throws Exception {
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", """
                    actions:
                      start: [ { send_message: "<green>Go" } ]
                      objective_progress: [ { send_message: "+1" } ]
                      objective_complete: [ { send_message: "<green>Done" } ]
                """));

        QuestConfigDTO mine = load().groups().get("daily").quests().getFirst();

        assertEquals(java.util.List.of(java.util.Map.of("send_message", "<green>Go")), mine.hooks().start());
        assertEquals(java.util.List.of(java.util.Map.of("send_message", "+1")), mine.hooks().objectiveProgress());
        assertEquals(java.util.List.of(java.util.Map.of("send_message", "<green>Done")), mine.hooks().objectiveComplete());
        assertEquals(java.util.List.of(), mine.rewards());
    }

    @Test
    void questOnAndRewardsKeysAreStillReadAsTheOldNamesOfActions() throws Exception {
        Files.writeString(project.resolve("quest_groups/old.yaml"), group("G2", "Q2", """
                    on:
                      complete: [ { send_message: "<green>Bravo" } ]
                """));
        assertEquals(java.util.List.of(java.util.Map.of("send_message", "<green>Bravo")),
                load().groups().get("old").quests().getFirst().rewards());
        Files.delete(project.resolve("quest_groups/old.yaml"));


        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", """
                    rewards: [ { send_message: "<green>Bravo" } ]
                """));

        QuestConfigDTO mine = load().groups().get("daily").quests().getFirst();

        assertEquals(java.util.List.of(java.util.Map.of("send_message", "<green>Bravo")), mine.rewards());
    }

    @Test
    void groupHooksAreReadFromActions() throws Exception {
        Files.writeString(project.resolve("quest_groups/daily.yaml"),
                group("G1", "Q1", "").replace("on:\n  quest_start", "actions:\n  quest_start"));

        assertEquals(java.util.Map.of("send_message", "<green>Go"),
                load().groups().get("daily").actions().questStart().getFirst());
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

    // ------------------------------------------------------------ every project

    private static Path newProject(Path parent, String lang) throws Exception {
        Files.createDirectories(parent.resolve("quest_groups"));
        Files.writeString(parent.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: " + lang + "\n");
        return parent;
    }

    @Test
    void readsTheGroupsOfEveryProjectEachInItsOwnLanguage(@TempDir Path other) throws Exception {
        Path guilds = newProject(other, "fr");
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", ""));
        Files.writeString(guilds.resolve("quest_groups/guild_levels.yaml"), group("G2", "Q2", ""));

        QuestConfigLoad load = new QuestGroupDocumentRepository(
                ProjectFiles.directories(java.util.Map.of("quests", project, "guildes", guilds))).loadNow();

        assertEquals(java.util.List.of(), load.failures());
        assertEquals("quests", load.groups().get("daily").sourceProject());
        assertEquals("Daily", load.groups().get("daily").name());
        assertEquals("guildes", load.groups().get("guild_levels").sourceProject());
        assertEquals("Quotidiennes", load.groups().get("guild_levels").name());
    }

    @Test
    void aGroupKeyHeldByTwoProjectsLoadsInNeither(@TempDir Path other) throws Exception {
        Path guilds = newProject(other, "en");
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", ""));
        Files.writeString(guilds.resolve("quest_groups/daily.yaml"), group("G2", "Q2", ""));
        Files.writeString(guilds.resolve("quest_groups/weekly.yaml"), group("G3", "Q3", ""));

        QuestConfigLoad load = new QuestGroupDocumentRepository(
                ProjectFiles.directories(java.util.Map.of("quests", project, "guildes", guilds))).loadNow();

        assertEquals(java.util.Set.of("weekly"), load.groups().keySet());
        assertEquals(1, load.failures().size());
        String failure = load.failures().getFirst();
        assertTrue(failure.contains("guildes/quest_groups/daily.yaml"), failure);
        assertTrue(failure.contains("quests/quest_groups/daily.yaml"), failure);
        assertTrue(failure.contains("unique on the server"), failure);
    }

    @Test
    void aUidIsUniqueAcrossProjects(@TempDir Path other) throws Exception {
        Path guilds = newProject(other, "en");
        Files.writeString(guilds.resolve("quest_groups/a.yaml"), group("G1", "Q1", ""));
        Files.writeString(project.resolve("quest_groups/b.yaml"), group("G1", "Q2", ""));

        QuestConfigLoad load = new QuestGroupDocumentRepository(
                ProjectFiles.directories(java.util.Map.of("quests", project, "guildes", guilds))).loadNow();

        assertEquals(java.util.Set.of("a"), load.groups().keySet());
        assertTrue(load.failures().getFirst().startsWith("quests/quest_groups/b.yaml: uid G1 of b is already used by a"),
                load.failures().toString());
    }

    @Test
    void theFingerprintFollowsTheGroupsOfOtherProjects(@TempDir Path other) throws Exception {
        Path guilds = newProject(other, "en");
        QuestGroupDocumentRepository repository = new QuestGroupDocumentRepository(
                ProjectFiles.directories(java.util.Map.of("quests", project, "guildes", guilds)));
        Files.writeString(project.resolve("quest_groups/daily.yaml"), group("G1", "Q1", ""));
        String before = repository.fingerprint();

        Files.writeString(guilds.resolve("quest_groups/levels.yaml"), group("G2", "Q2", ""));

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
