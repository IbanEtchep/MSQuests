package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerProfileSqlRepositoryTest extends AbstractDatabaseTest {

    private PlayerProfileSqlRepository mysqlRepository;
    private PlayerProfileSqlRepository h2Repository;

    @BeforeEach
    void setUp() {
        h2Repository = new PlayerProfileSqlRepository(h2DbAccess);
        mysqlRepository = new PlayerProfileSqlRepository(mysqlDbAccess);
    }

    @Test
    void testSaveAndGetPlayerProfileInH2() throws ExecutionException, InterruptedException {
        UUID playerId = UUID.randomUUID();
        UUID trackedQuestId = UUID.randomUUID();
        PlayerProfileDTO profile = new PlayerProfileDTO(playerId, "", trackedQuestId);

        // Test save
        CompletableFuture<Void> saveFuture = h2Repository.save(profile);
        assertDoesNotThrow(() -> saveFuture.get());

        // Test get
        CompletableFuture<PlayerProfileDTO> getFuture = h2Repository.get(playerId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNotNull(retrievedProfile);
        assertEquals(profile.id(), retrievedProfile.id());
        assertEquals(profile.trackedQuestId(), retrievedProfile.trackedQuestId());
    }

    @Test
    void testSaveAndGetPlayerProfileWithNullQuestInH2() throws ExecutionException, InterruptedException {
        UUID playerId = UUID.randomUUID();
        PlayerProfileDTO profile = new PlayerProfileDTO(playerId, "", null);

        // Test save with null trackedQuestId
        CompletableFuture<Void> saveFuture = h2Repository.save(profile);
        assertDoesNotThrow(() -> saveFuture.get());

        // Test get
        CompletableFuture<PlayerProfileDTO> getFuture = h2Repository.get(playerId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNotNull(retrievedProfile);
        assertEquals(profile.id(), retrievedProfile.id());
        assertNull(retrievedProfile.trackedQuestId());
    }

    @Test
    void testNonExistentPlayerProfileReturnsNullInH2() throws ExecutionException, InterruptedException {
        UUID nonExistentId = UUID.randomUUID();
        CompletableFuture<PlayerProfileDTO> getFuture = h2Repository.get(nonExistentId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNull(retrievedProfile);
    }

    @Test
    void testSaveAndGetPlayerProfileInMySQL() throws ExecutionException, InterruptedException {
        UUID playerId = UUID.randomUUID();
        UUID trackedQuestId = UUID.randomUUID();
        PlayerProfileDTO profile = new PlayerProfileDTO(playerId, "", trackedQuestId);

        // Test save
        CompletableFuture<Void> saveFuture = mysqlRepository.save(profile);
        assertDoesNotThrow(() -> saveFuture.get());

        // Test get
        CompletableFuture<PlayerProfileDTO> getFuture = mysqlRepository.get(playerId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNotNull(retrievedProfile);
        assertEquals(profile.id(), retrievedProfile.id());
        assertEquals(profile.trackedQuestId(), retrievedProfile.trackedQuestId());
    }

    @Test
    void testSaveAndGetPlayerProfileWithNullQuestInMySQL() throws ExecutionException, InterruptedException {
        UUID playerId = UUID.randomUUID();
        PlayerProfileDTO profile = new PlayerProfileDTO(playerId,"", null);

        // Test save with null trackedQuestId
        CompletableFuture<Void> saveFuture = mysqlRepository.save(profile);
        assertDoesNotThrow(() -> saveFuture.get());

        // Test get
        CompletableFuture<PlayerProfileDTO> getFuture = mysqlRepository.get(playerId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNotNull(retrievedProfile);
        assertEquals(profile.id(), retrievedProfile.id());
        assertNull(retrievedProfile.trackedQuestId());
    }

    @Test
    void testNonExistentPlayerProfileReturnsNullInMySQL() throws ExecutionException, InterruptedException {
        UUID nonExistentId = UUID.randomUUID();
        CompletableFuture<PlayerProfileDTO> getFuture = mysqlRepository.get(nonExistentId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNull(retrievedProfile);
    }

    @Test
    void testUpdatePlayerProfile() throws ExecutionException, InterruptedException {
        UUID playerId = UUID.randomUUID();
        UUID initialQuestId = UUID.randomUUID();
        UUID updatedQuestId = UUID.randomUUID();

        // First save with initial quest
        PlayerProfileDTO initialProfile = new PlayerProfileDTO(playerId, "", initialQuestId);
        CompletableFuture<Void> initialSave = h2Repository.save(initialProfile);
        assertDoesNotThrow(() -> initialSave.get());

        // Update with new quest
        PlayerProfileDTO updatedProfile = new PlayerProfileDTO(playerId, "", updatedQuestId);
        CompletableFuture<Void> updateFuture = h2Repository.save(updatedProfile);
        assertDoesNotThrow(() -> updateFuture.get());

        // Verify update
        CompletableFuture<PlayerProfileDTO> getFuture = h2Repository.get(playerId);
        PlayerProfileDTO retrievedProfile = getFuture.get();

        assertNotNull(retrievedProfile);
        assertEquals(playerId, retrievedProfile.id());
        assertEquals(updatedQuestId, retrievedProfile.trackedQuestId());
    }
}
