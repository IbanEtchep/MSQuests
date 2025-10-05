package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class QuestSqlRepositoryTest extends AbstractDatabaseTest {

    private QuestSqlRepository h2Repository;
    private QuestSqlRepository mysqlRepository;

    private final UUID actorId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID actorId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final UUID questId1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID questId2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID questId3 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() throws SQLException {
        h2Repository = new QuestSqlRepository(h2DbAccess);
        mysqlRepository = new QuestSqlRepository(mysqlDbAccess);

        setupFixtures();
    }

    private void setupFixtures() throws SQLException {
        ActorFixture actorFixture = new ActorFixture()
                .addActor(actorId1, "PLAYER")
                .addActor(actorId2, "NPC");
        addFixture(actorFixture);

        QuestFixture questFixture = new QuestFixture()
                .addQuest(questId1, "daily_quest", "daily", "IN_PROGRESS", actorId1)
                .addQuest(questId2, "main_story", "story", "COMPLETED", actorId1)
                .addQuest(questId3, "side_quest", "side", "FAILED", actorId2);
        addFixture(questFixture);

        ObjectiveFixture objectiveFixture = new ObjectiveFixture()
                .addObjective("collect_items", "ITEM", "IN_PROGRESS", "5", questId1)
                .addObjective("defeat_boss", "BOSS", "COMPLETED", "1", questId2);
        addFixture(objectiveFixture);

        loadFixtures();
    }

    @Test
    void testGetQuestsByActorH2() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<UUID, QuestDTO>> actorQuestsFuture = h2Repository.getAllByActor(actorId1);
        Map<UUID, QuestDTO> actorQuests = actorQuestsFuture.get();

        assertNotNull(actorQuests);
        assertEquals(2, actorQuests.size());
        assertTrue(actorQuests.containsKey(questId1));
        assertTrue(actorQuests.containsKey(questId2));

        QuestDTO dailyQuest = actorQuests.get(questId1);
        assertEquals("daily_quest", dailyQuest.questKey());
        assertEquals("IN_PROGRESS", dailyQuest.status().toString());

        actorQuestsFuture = h2Repository.getAllByActor(actorId2);
        actorQuests = actorQuestsFuture.get();

        assertNotNull(actorQuests);
        assertEquals(1, actorQuests.size());
        assertTrue(actorQuests.containsKey(questId3));
    }

    @Test
    void testGetQuestsByActorMySQL() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<UUID, QuestDTO>> actorQuestsFuture = mysqlRepository.getAllByActor(actorId1);
        Map<UUID, QuestDTO> actorQuests = actorQuestsFuture.get();

        assertNotNull(actorQuests);
        assertEquals(2, actorQuests.size());
        assertTrue(actorQuests.containsKey(questId1));
        assertTrue(actorQuests.containsKey(questId2));

        actorQuestsFuture = mysqlRepository.getAllByActor(actorId2);
        actorQuests = actorQuestsFuture.get();

        assertNotNull(actorQuests);
        assertEquals(1, actorQuests.size());
        assertTrue(actorQuests.containsKey(questId3));
    }

    @Test
    void testNonExistentActorReturnsEmptyMap() throws ExecutionException, InterruptedException {
        UUID nonExistentId = UUID.randomUUID();
        CompletableFuture<Map<UUID, QuestDTO>> actorQuestsFuture = h2Repository.getAllByActor(nonExistentId);
        Map<UUID, QuestDTO> actorQuests = actorQuestsFuture.get();

        assertNotNull(actorQuests);
        assertTrue(actorQuests.isEmpty());
    }

    @Test
    void testQuestsHaveObjectivesH2() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<UUID, QuestDTO>> actorQuestsFuture = h2Repository.getAllByActor(actorId1);
        Map<UUID, QuestDTO> actorQuests = actorQuestsFuture.get();

        // Check that questId1 has objectiveId1
        QuestDTO dailyQuest = actorQuests.get(questId1);
        assertNotNull(dailyQuest);
        assertNotNull(dailyQuest.objectives());
        assertEquals(1, dailyQuest.objectives().size());
        assertTrue(dailyQuest.objectives().containsKey("collect_items"));

        // Validate objective properties
        QuestObjectiveDTO objective = dailyQuest.objectives().get("collect_items");
        assertEquals("collect_items", objective.objectiveKey());
        assertEquals("IN_PROGRESS", objective.objectiveStatus().toString());

        // Check that questId2 has objectiveId2
        QuestDTO mainStoryQuest = actorQuests.get(questId2);
        assertNotNull(mainStoryQuest);
        assertNotNull(mainStoryQuest.objectives());
        assertEquals(1, mainStoryQuest.objectives().size());
        assertTrue(mainStoryQuest.objectives().containsKey("defeat_boss"));
    }

    @Test
    void testQuestsHaveObjectivesMySQL() throws ExecutionException, InterruptedException {
        CompletableFuture<Map<UUID, QuestDTO>> actorQuestsFuture = mysqlRepository.getAllByActor(actorId1);
        Map<UUID, QuestDTO> actorQuests = actorQuestsFuture.get();

        // Check that questId1 has objectiveId1
        QuestDTO dailyQuest = actorQuests.get(questId1);
        assertNotNull(dailyQuest);
        assertNotNull(dailyQuest.objectives());
        assertEquals(1, dailyQuest.objectives().size());
        assertTrue(dailyQuest.objectives().containsKey("collect_items"));

        // Validate objective properties
        QuestObjectiveDTO objective = dailyQuest.objectives().get("collect_items");
        assertEquals("collect_items", objective.objectiveKey());
        assertEquals("IN_PROGRESS", objective.objectiveStatus().toString());

        // Check that questId2 has objectiveId2
        QuestDTO mainStoryQuest = actorQuests.get(questId2);
        assertNotNull(mainStoryQuest);
        assertNotNull(mainStoryQuest.objectives());
        assertEquals(1, mainStoryQuest.objectives().size());
        assertTrue(mainStoryQuest.objectives().containsKey("defeat_boss"));
    }

    @Test
    void testQuestGroupKeyIsLoaded() throws ExecutionException, InterruptedException {
        // Test H2
        Map<UUID, QuestDTO> h2Quests = h2Repository.getAllByActor(actorId1).get();
        assertEquals("daily", h2Quests.get(questId1).groupKey());
        assertEquals("story", h2Quests.get(questId2).groupKey());

        // Test MySQL
        Map<UUID, QuestDTO> mysqlQuests = mysqlRepository.getAllByActor(actorId1).get();
        assertEquals("daily", mysqlQuests.get(questId1).groupKey());
        assertEquals("story", mysqlQuests.get(questId2).groupKey());

        // Test for actor 2
        Map<UUID, QuestDTO> actor2Quests = h2Repository.getAllByActor(actorId2).get();
        assertEquals("side", actor2Quests.get(questId3).groupKey());
    }

    @Test
    void testSaveNewQuestH2() throws ExecutionException, InterruptedException {
        UUID newQuestId = UUID.randomUUID();
        QuestDTO newQuest = new QuestDTO(
                newQuestId,
                "new_quest",
                "event",
                actorId1,
                QuestStatus.IN_PROGRESS,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                Map.of()
        );

        CompletableFuture<Void> saveFuture = h2Repository.save(newQuest);
        saveFuture.get(); // attend que l'opération soit terminée

        // Vérifie que la quête est bien enregistrée
        QuestDTO loaded = h2Repository.getAllByActor(actorId1).get().get(newQuestId);
        assertNotNull(loaded);
        assertEquals("new_quest", loaded.questKey());
        assertEquals("IN_PROGRESS", loaded.status().toString());
    }

    @Test
    void testUpdateExistingQuestH2() throws ExecutionException, InterruptedException {
        // Récupère une quête existante
        QuestDTO existing = h2Repository.getAllByActor(actorId1).get().get(questId1);

        // Change le status
        QuestDTO updated = new QuestDTO(
                existing.id(),
                existing.questKey(),
                existing.groupKey(),
                existing.actorId(),
                QuestStatus.COMPLETED,
                System.currentTimeMillis(),
                existing.createdAt(),
                System.currentTimeMillis(),
                existing.objectives()
        );

        h2Repository.save(updated).get();

        QuestDTO loaded = h2Repository.getAllByActor(actorId1).get().get(questId1);
        assertNotNull(loaded);
        assertEquals("COMPLETED", loaded.status().toString());
        assertEquals(existing.questKey(), loaded.questKey());
    }

    @Test
    void testSaveQuestWithObjectivesH2() throws ExecutionException, InterruptedException {
        UUID questId = UUID.randomUUID();
        QuestObjectiveDTO objective = new QuestObjectiveDTO(
                questId,
                "find_treasure",
                "ITEM",
                QuestObjectiveStatus.IN_PROGRESS,
                0
        );

        QuestDTO quest = new QuestDTO(
                questId,
                "treasure_quest",
                "event",
                actorId1,
                QuestStatus.IN_PROGRESS,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                Map.of(objective.objectiveKey(), objective)
        );

        h2Repository.save(quest).get();

        QuestDTO loaded = h2Repository.getAllByActor(actorId1).get().get(questId);
        assertNotNull(loaded);
        assertEquals(1, loaded.objectives().size());
        assertTrue(loaded.objectives().containsKey("find_treasure"));

        QuestObjectiveDTO loadedObjective = loaded.objectives().get("find_treasure");
        assertEquals("IN_PROGRESS", loadedObjective.objectiveStatus().toString());
    }
}