package com.github.ibanetchep.msquests.bukkit.repository;

import com.github.ibanetchep.msquests.core.dto.*;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class QuestConfigYamlRepositoryTest {

    @TempDir
    Path tempDir;

    private QuestConfigYamlRepository repository;
    private Path questsDir;
    private Path testFilePath;

    @BeforeEach
    public void setUp() throws IOException {
        questsDir = tempDir.resolve("quests");
        Files.createDirectories(questsDir);

        // Copier le fichier depuis les ressources
        Path resourceFile = Path.of("src/test/resources/quests/group_1.yml");
        testFilePath = questsDir.resolve("group_1.yml");
        Files.copy(resourceFile, testFilePath, StandardCopyOption.REPLACE_EXISTING);

        repository = new QuestConfigYamlRepository(questsDir, Logger.getLogger(QuestConfigYamlRepositoryTest.class.getName()));
    }

    @Test
    public void testGetAllGroups() throws Exception {
        CompletableFuture<Map<String, QuestGroupConfigDTO>> future = repository.getAllGroups();
        Map<String, QuestGroupConfigDTO> result = future.get();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have exactly 1 group");

        QuestGroupConfigDTO group = result.get("group_1");
        assertNotNull(group, "Group 'group_1' should exist");
        assertEquals("group_1", group.key());
        assertEquals("groupe 1", group.name());
        assertEquals("Description du groupe", group.description());
        assertEquals("CHAINED", group.distributionMode());
        assertEquals(3, group.maxActive());
        assertEquals(10, group.maxPerPeriod());
        assertEquals("0 0 * * *", group.resetCron());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), group.startAt());
        assertEquals(Instant.parse("2024-12-31T23:59:59Z"), group.endAt());

        List<QuestConfigDTO> quests = group.quests();
        assertNotNull(quests, "Quests should not be null");
        assertEquals(2, quests.size(), "Should have 2 quests");

        // ===== QUEST 1 =====
        QuestConfigDTO quest1 = group.getQuest("quest_1");
        assertNotNull(quest1, "Quest 1 should exist");
        assertEquals("quest_1", quest1.key());
        assertEquals("First Quest", quest1.name());
        assertEquals("This is the first quest", quest1.description());
        assertEquals(120, quest1.duration());

        // Quest 1 - Rewards
        assertEquals(2, quest1.rewards().size(), "Quest 1 should have 2 rewards");

        QuestActionDTO reward1_1 = quest1.rewards().get(0);
        assertEquals("command", reward1_1.type());
        assertEquals("Command Reward", reward1_1.name());
        assertEquals("give %actor_name% diamond 1", reward1_1.config().get("command"));

        QuestActionDTO reward1_2 = quest1.rewards().get(1);
        assertEquals("give_item", reward1_2.type());
        assertEquals("Item Reward", reward1_2.name());
        assertEquals("DIAMOND", reward1_2.config().get("material"));
        assertEquals(10, ((Number) reward1_2.config().get("amount")).intValue());

        // Quest 1 - Stages
        List<QuestStageConfigDTO> stages1 = quest1.stages();
        assertEquals(2, stages1.size(), "Quest 1 should have 2 stages");

        // Quest 1 - Stage 1
        QuestStageConfigDTO stage1_1 = quest1.getStage("stage_1");
        assertNotNull(stage1_1, "Stage 1 should exist");
        assertEquals("stage_1", stage1_1.key());
        assertEquals("Premier stage", stage1_1.name());
        assertEquals(Flow.PARALLEL, stage1_1.flow());

        List<QuestObjectiveConfigDTO> objectives1_1 = stage1_1.objectives();
        assertEquals(2, objectives1_1.size(), "Stage 1 should have 2 objectives");

        QuestObjectiveConfigDTO obj1_1_1 = stage1_1.getObjective("objective_1");
        assertNotNull(obj1_1_1, "Objective 1 should exist");
        assertEquals("objective_1", obj1_1_1.key());
        assertEquals("block_break", obj1_1_1.type());
        assertEquals("STONE", obj1_1_1.config().get("material"));
        assertEquals(10, ((Number) obj1_1_1.config().get("amount")).intValue());

        QuestObjectiveConfigDTO obj1_1_2 = stage1_1.getObjective("objective_2");
        assertNotNull(obj1_1_2, "Objective 2 should exist");
        assertEquals("objective_2", obj1_1_2.key());
        assertEquals("kill_entity", obj1_1_2.type());
        assertEquals("ZOMBIE", obj1_1_2.config().get("entity_type"));
        assertEquals(5, ((Number) obj1_1_2.config().get("amount")).intValue());

        // Quest 1 - Stage 2
        QuestStageConfigDTO stage1_2 = quest1.getStage("stage_2");
        assertNotNull(stage1_2, "Stage 2 should exist");
        assertEquals("stage_2", stage1_2.key());
        assertEquals("Deuxième stage", stage1_2.name());
        assertEquals(Flow.PARALLEL, stage1_2.flow());

        List<QuestObjectiveConfigDTO> objectives1_2 = stage1_2.objectives();
        assertEquals(1, objectives1_2.size(), "Stage 2 should have 1 objective");

        QuestObjectiveConfigDTO obj1_2_1 = stage1_2.getObjective("stage_2_1");
        assertNotNull(obj1_2_1, "Stage 2 objective should exist");
        assertEquals("stage_2_1", obj1_2_1.key());
        assertEquals("block_break", obj1_2_1.type());
        assertEquals("DIRT", obj1_2_1.config().get("material"));
        assertEquals(5, ((Number) obj1_2_1.config().get("amount")).intValue());

        // ===== QUEST 2 =====
        QuestConfigDTO quest2 = group.getQuest("quest_2");
        assertNotNull(quest2, "Quest 2 should exist");
        assertEquals("quest_2", quest2.key());
        assertEquals("Second Quest", quest2.name());
        assertEquals("This is the second quest", quest2.description());
        assertEquals(180, quest2.duration());

        // Quest 2 - Rewards
        assertEquals(2, quest2.rewards().size(), "Quest 2 should have 2 rewards");

        QuestActionDTO reward2_1 = quest2.rewards().get(0);
        assertEquals("command", reward2_1.type());
        assertEquals("Command Reward", reward2_1.name());
        assertEquals("give %actor_name% dirt 5", reward2_1.config().get("command"));

        QuestActionDTO reward2_2 = quest2.rewards().get(1);
        assertEquals("give_item", reward2_2.type());
        assertEquals("Item Reward", reward2_2.name());
        assertEquals("DIRT", reward2_2.config().get("material"));
        assertEquals(1, ((Number) reward2_2.config().get("amount")).intValue());

        // Quest 2 - Stages
        List<QuestStageConfigDTO> stages2 = quest2.stages();
        assertEquals(1, stages2.size(), "Quest 2 should have 1 stage");

        QuestStageConfigDTO stage2_1 = quest2.getStage("stage_1");
        assertNotNull(stage2_1, "Stage 1 should exist");
        assertEquals("stage_1", stage2_1.key());
        assertEquals("Stage unique", stage2_1.name());
        assertEquals(Flow.PARALLEL, stage2_1.flow());

        List<QuestObjectiveConfigDTO> objectives2_1 = stage2_1.objectives();
        assertEquals(1, objectives2_1.size());

        QuestObjectiveConfigDTO obj2_1_1 = stage2_1.getObjective("objective_1");
        assertNotNull(obj2_1_1, "Objective 1 should exist");
        assertEquals("objective_1", obj2_1_1.key());
        assertEquals("kill_entity", obj2_1_1.type());
        assertEquals("PIG", obj2_1_1.config().get("entity_type"));
        assertEquals(1, ((Number) obj2_1_1.config().get("amount")).intValue());
    }

    @Test
    public void testLoadGroup() throws IOException {
        QuestGroupConfigDTO group = repository.loadGroup(testFilePath);

        assertNotNull(group, "Group should not be null");
        assertEquals("group_1", group.key());
        assertEquals("groupe 1", group.name());
        assertEquals(2, group.quests().size());

        // Verify path is stored
        Path storedPath = repository.getGroupPath("group_1");
        assertNotNull(storedPath, "Path should be stored");
        assertEquals(testFilePath, storedPath);
    }

    @Test
    public void testLoadGroupWithInvalidFile() {
        Path invalidPath = questsDir.resolve("non_existent.yml");

        assertThrows(IOException.class, () -> {
            repository.loadGroup(invalidPath);
        }, "Should throw IOException for non-existent file");
    }

    @Test
    public void testSaveGroup() throws IOException {
        // Load existing group
        QuestGroupConfigDTO originalGroup = repository.loadGroup(testFilePath);

        // Modify it
        QuestGroupConfigDTO modifiedGroup = new QuestGroupConfigDTO(
                originalGroup.key(),
                "Modified Name",
                "Modified Description",
                originalGroup.quests(),
                originalGroup.distributionMode(),
                5, // Changed from 3
                20, // Changed from 10
                originalGroup.resetCron(),
                originalGroup.startAt(),
                originalGroup.endAt()
        );

        // Save
        repository.saveGroup(modifiedGroup);

        // Verify file exists
        assertTrue(Files.exists(testFilePath), "File should exist after save");

        // Reload and verify changes
        QuestGroupConfigDTO reloadedGroup = repository.loadGroup(testFilePath);
        assertEquals("Modified Name", reloadedGroup.name());
        assertEquals("Modified Description", reloadedGroup.description());
        assertEquals(5, reloadedGroup.maxActive());
        assertEquals(20, reloadedGroup.maxPerPeriod());

        // Verify quests are preserved
        assertEquals(2, reloadedGroup.quests().size());
    }

    @Test
    public void testSaveNewGroup() throws IOException {
        // Create new group with empty list
        QuestGroupConfigDTO newGroup = new QuestGroupConfigDTO(
                "new_group",
                "New Group",
                "A brand new group",
                List.of(),  // ✅ Empty list
                "RANDOM",
                1,
                5,
                "0 0 * * *",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z")
        );

        // Save (path should be auto-created)
        repository.saveGroup(newGroup);

        // Verify file was created
        Path expectedPath = questsDir.resolve("new_group.yml");
        assertTrue(Files.exists(expectedPath), "New file should be created");

        // Reload and verify
        QuestGroupConfigDTO reloadedGroup = repository.loadGroup(expectedPath);
        assertEquals("new_group", reloadedGroup.key());
        assertEquals("New Group", reloadedGroup.name());
        assertEquals("A brand new group", reloadedGroup.description());
        assertEquals(0, reloadedGroup.quests().size());
    }

    @Test
    public void testSaveAndReloadCompleteGroup() throws IOException {
        // Create a complete quest group from scratch
        Map<String, Object> objectiveConfig = new HashMap<>();
        objectiveConfig.put("material", "DIAMOND_ORE");
        objectiveConfig.put("amount", 64);

        QuestObjectiveConfigDTO objective = new QuestObjectiveConfigDTO(
                "mine_diamonds",
                "block_break",
                objectiveConfig
        );

        QuestStageConfigDTO stage = new QuestStageConfigDTO(
                "mining_stage",
                "Mining Stage",
                Flow.SEQUENTIAL,
                List.of(objective)  // ✅ List
        );

        Map<String, Object> rewardConfig = new HashMap<>();
        rewardConfig.put("command", "give %player% diamond_pickaxe 1");

        QuestActionDTO reward = new QuestActionDTO(
                "command",
                "Diamond Pickaxe Reward",
                rewardConfig
        );

        QuestConfigDTO quest = new QuestConfigDTO(
                "mining_quest",
                "Mining Quest",
                "Mine some diamonds",
                3600L,
                List.of(reward),
                List.of(stage)  // ✅ List
        );

        QuestGroupConfigDTO group = new QuestGroupConfigDTO(
                "test_group",
                "Test Group",
                "Test Description",
                List.of(quest),  // ✅ List
                "ALL",
                10,
                50,
                "0 0 * * 0",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-12-31T23:59:59Z")
        );

        // Save
        repository.saveGroup(group);

        // Reload
        Path savedPath = questsDir.resolve("test_group.yml");
        QuestGroupConfigDTO reloadedGroup = repository.loadGroup(savedPath);

        // Verify everything
        assertEquals("test_group", reloadedGroup.key());
        assertEquals("Test Group", reloadedGroup.name());
        assertEquals(1, reloadedGroup.quests().size());

        // ✅ Use helper
        QuestConfigDTO reloadedQuest = reloadedGroup.getQuest("mining_quest");
        assertNotNull(reloadedQuest, "Mining quest should exist");
        assertEquals("mining_quest", reloadedQuest.key());
        assertEquals("Mining Quest", reloadedQuest.name());
        assertEquals(1, reloadedQuest.stages().size());

        QuestStageConfigDTO reloadedStage = reloadedQuest.getStage("mining_stage");
        assertNotNull(reloadedStage);
        assertEquals("Mining Stage", reloadedStage.name());
        assertEquals(Flow.SEQUENTIAL, reloadedStage.flow());

        QuestObjectiveConfigDTO reloadedObjective = reloadedStage.getObjective("mine_diamonds");
        assertNotNull(reloadedObjective);
        assertEquals("block_break", reloadedObjective.type());
        assertEquals("DIAMOND_ORE", reloadedObjective.config().get("material"));
        assertEquals(64, ((Number) reloadedObjective.config().get("amount")).intValue());

        QuestActionDTO reloadedReward = reloadedQuest.rewards().get(0);
        assertEquals("command", reloadedReward.type());
        assertEquals("give %player% diamond_pickaxe 1", reloadedReward.config().get("command"));
    }

    @Test
    public void testGetGroupPath() throws IOException {
        // Before loading
        Path path = repository.getGroupPath("group_1");
        assertNull(path, "Path should be null before loading");

        // Load the group
        repository.loadGroup(testFilePath);

        // After loading
        path = repository.getGroupPath("group_1");
        assertNotNull(path, "Path should not be null after loading");
        assertEquals(testFilePath, path);
    }

    @Test
    public void testMultipleGroupsInDifferentFiles() throws IOException {
        // Create second group file with list format
        Path secondFilePath = questsDir.resolve("group_2.yml");
        String yamlContent = """
            key: group_2
            name: "Second Group"
            description: "Another group"
            distribution_mode: RANDOM
            max_active: 1
            max_per_period: 3
            reset_cron: "0 0 * * *"
            quests: []
            """;  // ✅ Empty list
        Files.writeString(secondFilePath, yamlContent);

        // Load all groups
        CompletableFuture<Map<String, QuestGroupConfigDTO>> future = repository.getAllGroups();
        Map<String, QuestGroupConfigDTO> result = future.join();

        assertEquals(2, result.size(), "Should have 2 groups");
        assertTrue(result.containsKey("group_1"));
        assertTrue(result.containsKey("group_2"));

        assertEquals("groupe 1", result.get("group_1").name());
        assertEquals("Second Group", result.get("group_2").name());
    }

    @Test
    public void testIgnoreBackupFiles() throws IOException {
        // Create a backup file
        Path backupFile = questsDir.resolve("group_1.yml.bak");
        Files.copy(testFilePath, backupFile);

        // Load all groups
        CompletableFuture<Map<String, QuestGroupConfigDTO>> future = repository.getAllGroups();
        Map<String, QuestGroupConfigDTO> result = future.join();

        // Should only load the .yml file, not the .bak
        assertEquals(1, result.size(), "Should ignore .bak files");
        assertTrue(result.containsKey("group_1"));
    }

    @Test
    public void testConfigStructure() throws IOException {
        QuestGroupConfigDTO group = repository.loadGroup(testFilePath);

        QuestConfigDTO quest1 = group.getQuest("quest_1");
        assertNotNull(quest1, "Quest 1 should exist");

        QuestStageConfigDTO stage1 = quest1.getStage("stage_1");
        assertNotNull(stage1, "Stage 1 should exist");

        QuestObjectiveConfigDTO objective1 = stage1.getObjective("objective_1");
        assertNotNull(objective1, "Objective 1 should exist");

        // Config should contain specific fields, not generic ones
        assertNotNull(objective1.config().get("material"), "Config should contain 'material'");
        assertNotNull(objective1.config().get("amount"), "Config should contain 'amount'");
        assertNull(objective1.config().get("key"), "Config should NOT contain 'key'");
        assertNull(objective1.config().get("type"), "Config should NOT contain 'type'");

        // Same for rewards
        QuestActionDTO reward1 = quest1.rewards().get(0);
        assertNotNull(reward1.config().get("command"), "Config should contain 'command'");
        assertNull(reward1.config().get("type"), "Config should NOT contain 'type'");
        assertNull(reward1.config().get("name"), "Config should NOT contain 'name'");
    }
}
