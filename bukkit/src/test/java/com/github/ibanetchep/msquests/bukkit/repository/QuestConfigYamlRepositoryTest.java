package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
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
    public void testGetAllGroups() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<String, QuestGroupDTO>> future = repository.getAllGroups();
        Map<String, QuestGroupDTO> result = future.get();

        assertNotNull(result);
        assertEquals(1, result.size());
        
        QuestGroupDTO group = result.get("group_1");
        assertNotNull(group);
        
        Map<String, QuestConfigDTO> quests = group.quests();
        assertNotNull(quests);
        assertEquals(2, quests.size());

        QuestConfigDTO quest1 = quests.get("quest_1");
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

        QuestConfigDTO quest2 = quests.get("quest_2");
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
    public void testLoadFileGroups() {
        List<QuestGroupDTO> loadedGroups = repository.loadFileGroups(testFilePath);

        assertEquals(1, loadedGroups.size());
        
        QuestGroupDTO group = loadedGroups.get(0);
        assertEquals("group_1", group.key());
        
        Map<String, QuestConfigDTO> quests = group.quests();
        assertEquals(2, quests.size());

        QuestConfigDTO quest1 = quests.get("quest_1");
        QuestConfigDTO quest2 = quests.get("quest_2");

        assertNotNull(quest1);
        assertNotNull(quest2);

        assertEquals("First Quest", quest1.name());
        assertEquals(2, quest1.objectives().size());
        assertEquals("group_1", quest1.groupKey());

        assertEquals("Second Quest", quest2.name());
        assertEquals(1, quest2.objectives().size());
        assertEquals("group_1", quest2.groupKey());
    }
}