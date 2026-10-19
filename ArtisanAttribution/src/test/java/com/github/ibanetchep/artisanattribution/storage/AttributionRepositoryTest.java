package com.github.ibanetchep.artisanattribution.storage;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributionRepositoryTest {

    private AttributionRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        repository = new AttributionRepository(dataSource);
        repository.migrate();
    }

    @Test
    void migratingTwiceChangesNothing() throws Exception {
        repository.migrate();
        UUID id = UUID.randomUUID();
        repository.answer(id, "discord", 1);
        repository.migrate();
        assertEquals("discord", repository.find(id).source());
    }

    @Test
    void anUnknownPlayerHasNoRow() throws Exception {
        PlayerAttribution player = repository.find(UUID.randomUUID());
        assertFalse(player.stored());
        assertFalse(player.answered());
        assertFalse(player.everPrompted());
    }

    @Test
    void countsThePrompts() throws Exception {
        UUID id = UUID.randomUUID();
        repository.markPrompted(id, 10);
        repository.markPrompted(id, 20);
        PlayerAttribution player = repository.find(id);
        assertEquals(2, player.prompts());
        assertEquals(20L, player.promptedAt());
        assertNull(player.source());
    }

    @Test
    void theFirstAnswerIsKept() throws Exception {
        UUID id = UUID.randomUUID();
        repository.markPrompted(id, 10);
        assertEquals(AnswerResult.SAVED, repository.answer(id, "tiktok", 30));
        assertEquals(AnswerResult.ALREADY_ANSWERED, repository.answer(id, "discord", 40));

        PlayerAttribution player = repository.find(id);
        assertEquals("tiktok", player.source());
        assertEquals(30L, player.answeredAt());
        assertEquals(1, player.prompts());
    }

    @Test
    void answersWithoutHavingBeenPrompted() throws Exception {
        UUID id = UUID.randomUUID();
        assertEquals(AnswerResult.SAVED, repository.answer(id, "friend", 5));
        assertEquals(AnswerResult.ALREADY_ANSWERED, repository.answer(id, "friend", 6));
        assertEquals("friend", repository.find(id).source());
    }

    @Test
    void resetForgetsTheAnswerAndKeepsTheRow() throws Exception {
        UUID id = UUID.randomUUID();
        assertFalse(repository.reset(id));
        repository.markPrompted(id, 1);
        repository.answer(id, "youtube", 2);

        assertTrue(repository.reset(id));
        PlayerAttribution player = repository.find(id);
        assertTrue(player.stored());
        assertFalse(player.answered());
        assertFalse(player.everPrompted());
        assertEquals(AnswerResult.SAVED, repository.answer(id, "discord", 3));
    }

    @Test
    void countsTheAnswersPerSource() throws Exception {
        repository.answer(UUID.randomUUID(), "discord", 1);
        repository.answer(UUID.randomUUID(), "discord", 1);
        repository.answer(UUID.randomUUID(), "tiktok", 1);
        repository.markPrompted(UUID.randomUUID(), 1);

        Map<String, Integer> counts = repository.countBySource();
        assertEquals(Map.of("discord", 2, "tiktok", 1), counts);
        assertEquals("discord", counts.keySet().iterator().next());
    }
}
