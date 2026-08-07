package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.mapper.QuestMapper;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.QuestRepository;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A quest removed from the YAML leaves its rows behind on purpose — deleting them
 * automatically would wipe player data on any config loading glitch. What must not happen is
 * one warning per actor per load: the fact is per quest key, so it is reported once.
 */
class QuestServiceOrphanLoggingTest {

    private final List<LogRecord> records = new ArrayList<>();
    private QuestService questService;
    private QuestRepository questRepository;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(QuestServiceOrphanLoggingTest.class.getName() + UUID.randomUUID());
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });

        QuestConfigRegistry questConfigRegistry = new QuestConfigRegistry();
        QuestGroupConfig daily = new QuestGroupConfig.Builder("daily", "Daily", "", "player").build();
        questConfigRegistry.registerQuestGroupConfig(daily);

        questRepository = mock(QuestRepository.class);
        RotationRepository rotationRepository = mock(RotationRepository.class);

        questService = new QuestService(
                logger,
                questConfigRegistry,
                questRepository,
                rotationRepository,
                mock(QuestFactory.class),
                new QuestRegistry(),
                mock(QuestMapper.class)
        );
    }

    @Test
    void reportsAnOrphanedQuestKeyOnlyOnce() {
        stubOrphanedQuest();

        questService.loadQuests(actor()).join();
        questService.loadQuests(actor()).join();
        questService.loadQuests(actor()).join();

        assertEquals(1, warnings().size(), () -> "expected a single warning, got " + warnings());
        assertTrue(warnings().getFirst().contains("kill_enderman_10"));
        assertTrue(warnings().getFirst().contains("daily"));
    }

    @Test
    void reportsAgainAfterConfigsAreReloaded() {
        stubOrphanedQuest();

        questService.loadQuests(actor()).join();
        questService.clearMissingConfigReports();
        questService.loadQuests(actor()).join();

        assertEquals(2, warnings().size());
    }

    @Test
    void reportsEachOrphanedKeySeparately() {
        when(questRepository.getAllByActor(any())).thenAnswer(invocation -> CompletableFuture.completedFuture(Map.of(
                UUID.randomUUID(), orphan("kill_enderman_10"),
                UUID.randomUUID(), orphan("mine_gold_64")
        )));

        questService.loadQuests(actor()).join();
        questService.loadQuests(actor()).join();

        assertEquals(2, warnings().size(), () -> "expected one warning per key, got " + warnings());
    }

    private void stubOrphanedQuest() {
        when(questRepository.getAllByActor(any())).thenAnswer(invocation ->
                CompletableFuture.completedFuture(Map.of(UUID.randomUUID(), orphan("kill_enderman_10"))));
    }

    private QuestDTO orphan(String questKey) {
        return new QuestDTO(
                UUID.randomUUID(), questKey, "daily", UUID.randomUUID(),
                QuestStatus.IN_PROGRESS, null, null, null, Map.of()
        );
    }

    private List<String> warnings() {
        return records.stream()
                .filter(record -> record.getLevel() == Level.WARNING)
                .map(LogRecord::getMessage)
                .toList();
    }

    private QuestActor actor() {
        return new QuestActor(UUID.randomUUID(), "Steve") {
            @Override
            public String getActorType() {
                return "player";
            }

            @Override
            public boolean isMember(UUID playerId) {
                return id.equals(playerId);
            }
        };
    }
}
