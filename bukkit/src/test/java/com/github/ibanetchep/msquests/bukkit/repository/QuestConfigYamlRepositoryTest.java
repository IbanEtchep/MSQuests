package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
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

        Map<String, QuestConfigDTO> quests = group.quests().stream()
                .collect(HashMap::new, (m, v) -> m.put(v.key(), v), HashMap::putAll);
        assertNotNull(quests);
        assertEquals(2, quests.size());

        // ---- Quest 1 ----
        QuestConfigDTO quest1 = quests.get("quest_1");
        assertNotNull(quest1);
        assertEquals("First Quest", quest1.name());
        assertEquals("This is the first quest", quest1.description());
        assertEquals(120, quest1.duration());
        assertEquals(2, quest1.rewards().size());
        assertEquals("Command Reward", quest1.rewards().getFirst().name());
        assertEquals("Item Reward", quest1.rewards().get(1).name());

        Map<String, QuestStageConfigDTO> stages1 = quest1.stages();
        assertEquals(1, stages1.size());
        QuestStageConfigDTO stage1 = stages1.get("stage_1");
        assertNotNull(stage1);
        assertEquals(2, stage1.objectives().size());

        QuestObjectiveConfigDTO objective1 = stage1.objectives().get("objective_1");
        assertNotNull(objective1);
        assertEquals("block_break", objective1.type());
        assertEquals("stone", objective1.config().get("material"));
        assertEquals(10, ((Number) objective1.config().get("amount")).intValue());

        // ---- Quest 2 ----
        QuestConfigDTO quest2 = quests.get("quest_2");
        assertNotNull(quest2);
        assertEquals("Second Quest", quest2.name());
        assertEquals("This is the second quest", quest2.description());
        assertEquals(180, quest2.duration());
        assertEquals(1, quest2.rewards().size());
        assertEquals("Command Reward", quest2.rewards().getFirst().name());

        Map<String, QuestStageConfigDTO> stages2 = quest2.stages();
        assertEquals(1, stages2.size());
        QuestStageConfigDTO stage2 = stages2.get("stage_1");
        assertNotNull(stage2);
        assertEquals(1, stage2.objectives().size());

        QuestObjectiveConfigDTO objective2_1 = stage2.objectives().get("objective_1");
        assertNotNull(objective2_1);
        assertEquals("explore", objective2_1.type());
        assertEquals("village", objective2_1.config().get("location"));
    }

    @Test
    public void testLoadFileGroups() {
        List<QuestGroupDTO> loadedGroups = repository.loadFileGroups(testFilePath);

        assertEquals(1, loadedGroups.size());

        QuestGroupDTO group = loadedGroups.get(0);
        assertEquals("group_1", group.key());

        Map<String, QuestConfigDTO> quests = group.quests().stream()
                .collect(HashMap::new, (m, v) -> m.put(v.key(), v), HashMap::putAll);
        assertEquals(2, quests.size());

        QuestConfigDTO quest1 = quests.get("quest_1");
        QuestConfigDTO quest2 = quests.get("quest_2");

        assertNotNull(quest1);
        assertNotNull(quest2);

        assertEquals("First Quest", quest1.name());
        assertEquals(1, quest1.stages().size());
        assertEquals(2, quest1.stages().get("stage_1").objectives().size());
        assertEquals("group_1", quest1.groupKey());

        assertEquals("Second Quest", quest2.name());
        assertEquals(1, quest2.stages().size());
        assertEquals(1, quest2.stages().get("stage_1").objectives().size());
        assertEquals("group_1", quest2.groupKey());
    }
}