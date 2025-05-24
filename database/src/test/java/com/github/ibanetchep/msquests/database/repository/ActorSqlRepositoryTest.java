package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ActorSqlRepositoryTest extends AbstractDatabaseTest {

    private ActorSqlRepository mysqlRepository;
    private ActorSqlRepository h2Repository;

    @BeforeEach
    void setUp() {
        h2Repository = new ActorSqlRepository(h2DbAccess);
        mysqlRepository = new ActorSqlRepository(mysqlDbAccess);
    }

    @Test
    void testAddAndGetActorInH2() throws ExecutionException, InterruptedException {
        UUID actorId = UUID.randomUUID();
        QuestActorDTO actor = new QuestActorDTO("PLAYER", actorId);

        CompletableFuture<Void> addFuture = h2Repository.add(actor);
        assertDoesNotThrow(() -> addFuture.get());

        CompletableFuture<QuestActorDTO> getFuture = h2Repository.get(actorId);
        QuestActorDTO retrievedActor = getFuture.get();

        assertNotNull(retrievedActor);
        assertEquals(actor.id(), retrievedActor.id());
        assertEquals(actor.actorType(), retrievedActor.actorType());
    }

    @Test
    void testNonExistentActorReturnsNullInH2() throws ExecutionException, InterruptedException {
        UUID nonExistentId = UUID.randomUUID();
        CompletableFuture<QuestActorDTO> getFuture = h2Repository.get(nonExistentId);
        QuestActorDTO retrievedActor = getFuture.get();

        assertNull(retrievedActor);
    }

    @Test
    void testAddAndGetActorInMySQL() throws ExecutionException, InterruptedException {
        UUID actorId = UUID.randomUUID();
        QuestActorDTO actor = new QuestActorDTO("NPC", actorId);

        CompletableFuture<Void> addFuture = mysqlRepository.add(actor);
        assertDoesNotThrow(() -> addFuture.get());

        CompletableFuture<QuestActorDTO> getFuture = mysqlRepository.get(actorId);
        QuestActorDTO retrievedActor = getFuture.get();

        assertNotNull(retrievedActor);
        assertEquals(actor.id(), retrievedActor.id());
        assertEquals(actor.actorType(), retrievedActor.actorType());
    }

    @Test
    void testNonExistentActorReturnsNullInMySQL() throws ExecutionException, InterruptedException {
        UUID nonExistentId = UUID.randomUUID();
        CompletableFuture<QuestActorDTO> getFuture = mysqlRepository.get(nonExistentId);
        QuestActorDTO retrievedActor = getFuture.get();

        assertNull(retrievedActor);
    }
}