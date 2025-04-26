package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class QuestConfigYamlRepositoryTest {

    @TempDir
    Path tempDir;

    private QuestConfigYamlRepository repository;
    private Path testFilePath;

    @BeforeEach
    public void setUp() throws IOException {
        Path questsDir = tempDir.resolve("quests");
        Files.createDirectories(questsDir);

        Path resourceFile = Path.of("src/test/resources/quests/quest_config_test.yml");
        testFilePath = questsDir.resolve("quest_config_test.yml");
        Files.copy(resourceFile, testFilePath, StandardCopyOption.REPLACE_EXISTING);

        repository = new QuestConfigYamlRepository(questsDir);
    }

    @Test
    public void testGetAll() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<String, QuestConfigDTO>> future = repository.getAll();
        Map<String, QuestConfigDTO> result = future.get();

        assertNotNull(result);
        assertEquals(2, result.size());

        QuestConfigDTO quest1 = result.get("quest_1");
        assertNotNull(quest1);
        assertEquals("First Quest", quest1.name());
        assertEquals("This is the first quest", quest1.description());
        assertEquals(120, quest1.duration());
        assertEquals(List.of("tag1", "tag2"), quest1.tags());
        assertEquals(List.of("reward1", "reward2"), quest1.rewards());

        Map<String, QuestObjectiveConfigDTO> objectives1 = quest1.objectives();
        assertEquals(2, objectives1.size());

        QuestObjectiveConfigDTO objective1 = objectives1.get("objective_1");
        assertNotNull(objective1);
        assertEquals("collect", objective1.config().get("type"));
        assertEquals("stone", objective1.config().get("item"));
        assertEquals(10, ((Number) objective1.config().get("quantity")).intValue());

        QuestConfigDTO quest2 = result.get("quest_2");
        assertNotNull(quest2);
        assertEquals("Second Quest", quest2.name());
        assertEquals("This is the second quest", quest2.description());
        assertEquals(180, quest2.duration());
        assertEquals(List.of("tag3"), quest2.tags());
        assertEquals(List.of("reward3"), quest2.rewards());

        Map<String, QuestObjectiveConfigDTO> objectives2 = quest2.objectives();
        assertEquals(1, objectives2.size());

        QuestObjectiveConfigDTO objective2_1 = objectives2.get("objective_1");
        assertNotNull(objective2_1);
        assertEquals("explore", objective2_1.config().get("type"));
        assertEquals("village", objective2_1.config().get("location"));
    }

    @Test
    public void testUpsert_UpdateExisting() throws ExecutionException, InterruptedException {
        Map<String, QuestConfigDTO> existingQuests = repository.getAll().get();
        QuestConfigDTO quest1 = existingQuests.get("quest_1");

        Map<String, QuestObjectiveConfigDTO> modifiedObjectives = new HashMap<>(quest1.objectives());
        QuestConfigDTO updatedQuest = new QuestConfigDTO(
                quest1.key(),
                "Updated Quest",
                quest1.description(),
                150L,
                List.of("tag1", "tag2", "new_tag"),
                quest1.rewards(),
                modifiedObjectives
        );

        repository.upsert(updatedQuest).get();

        Map<String, QuestConfigDTO> updatedQuests = repository.getAll().get();
        QuestConfigDTO modifiedQuest = updatedQuests.get("quest_1");

        assertNotNull(modifiedQuest);
        assertEquals("Updated Quest", modifiedQuest.name());
        assertEquals(150L, modifiedQuest.duration());
        assertTrue(modifiedQuest.tags().contains("new_tag"));
        assertEquals(3, modifiedQuest.tags().size());
    }

    @Test
    public void testUpsert_CreateNew() throws ExecutionException, InterruptedException {
        String newQuestKey = "quest_3";
        Map<String, QuestObjectiveConfigDTO> objectives = new HashMap<>();
        objectives.put("objective_1", new QuestObjectiveConfigDTO(
                "objective_1",
                Map.of(
                        "type", "craft",
                        "item", "wooden_sword",
                        "quantity", 1
                )
        ));

        QuestConfigDTO newQuest = new QuestConfigDTO(
                newQuestKey,
                "New Quest",
                "This is a new quest",
                200L,
                List.of("new", "test"),
                List.of("xp 100"),
                objectives
        );

        CompletableFuture<Void> upsertFuture = repository.upsert(newQuest);
        upsertFuture.get();

        Map<String, QuestConfigDTO> updatedQuests = repository.getAll().get();

        assertEquals(3, updatedQuests.size());
        assertTrue(updatedQuests.containsKey(newQuestKey));

        QuestConfigDTO createdQuest = updatedQuests.get(newQuestKey);
        assertEquals("New Quest", createdQuest.name());
        assertEquals("This is a new quest", createdQuest.description());
        assertEquals(200L, createdQuest.duration());
        assertEquals(List.of("new", "test"), createdQuest.tags());
        assertEquals(List.of("xp 100"), createdQuest.rewards());

        QuestObjectiveConfigDTO objective = createdQuest.objectives().get("objective_1");
        assertNotNull(objective);
        assertEquals("craft", objective.config().get("type"));
        assertEquals("wooden_sword", objective.config().get("item"));
        assertEquals(1, ((Number) objective.config().get("quantity")).intValue());
    }

    @Test
    public void testDelete() throws ExecutionException, InterruptedException {
        Map<String, QuestConfigDTO> initialQuests = repository.getAll().get();
        assertTrue(initialQuests.containsKey("quest_1"));

        CompletableFuture<Void> deleteFuture = repository.delete("quest_1");
        deleteFuture.get();

        Map<String, QuestConfigDTO> remainingQuests = repository.getAll().get();
        assertEquals(1, remainingQuests.size());
        assertFalse(remainingQuests.containsKey("quest_1"));
        assertTrue(remainingQuests.containsKey("quest_2"));
    }

    @Test
    public void testLoadFileQuests() {
        List<QuestConfigDTO> loadedQuests = repository.loadFileQuests(testFilePath);

        assertEquals(2, loadedQuests.size());

        QuestConfigDTO quest1 = loadedQuests.stream()
                .filter(q -> q.key().equals("quest_1"))
                .findFirst()
                .orElse(null);

        QuestConfigDTO quest2 = loadedQuests.stream()
                .filter(q -> q.key().equals("quest_2"))
                .findFirst()
                .orElse(null);

        assertNotNull(quest1);
        assertNotNull(quest2);

        assertEquals("First Quest", quest1.name());
        assertEquals(2, quest1.objectives().size());

        assertEquals("Second Quest", quest2.name());
        assertEquals(1, quest2.objectives().size());
    }
}