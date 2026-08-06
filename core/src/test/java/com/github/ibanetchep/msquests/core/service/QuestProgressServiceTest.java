package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pending progress is keyed by (questId, objectiveKey), not by objective identity:
 * a reconnect rebuilds every Quest/QuestObjective instance, so identity-keyed batches
 * would be applied to detached objects and lost.
 */
class QuestProgressServiceTest {

    static class TestObjectiveConfig extends QuestObjectiveConfig {
        private final int target;

        TestObjectiveConfig(String key, int target) {
            super(new QuestObjectiveConfigDTO(key, "test", Map.of()));
            this.target = target;
        }

        @Override
        public int getTarget() {
            return target;
        }

        @Override
        public QuestObjectiveConfigDTO toDTO() {
            return new QuestObjectiveConfigDTO(key, type, Map.of());
        }

        @Override
        public Map<String, String> getPlaceholders(Translator translator) {
            return Map.of();
        }
    }

    static class TestObjective extends AbstractQuestObjective<TestObjectiveConfig> {
        TestObjective(QuestStage stage, String key, int progress, int target) {
            super(stage, new TestObjectiveConfig(key, target), progress, QuestObjectiveStatus.IN_PROGRESS);
        }
    }

    static class TestActor extends QuestActor {
        TestActor(UUID id) {
            super(id, "TestActor");
        }

        @Override
        public String getActorType() {
            return "player";
        }

        @Override
        public boolean isMember(UUID playerId) {
            return false;
        }
    }

    private QuestProgressService progressService;
    private QuestLifecycleService lifecycleService;
    private QuestService questService;
    private EventDispatcher dispatcher;
    private QuestRegistry questRegistry;

    private QuestGroupConfig groupConfig;
    private QuestConfig questConfig;
    private TestActor actor;

    @BeforeEach
    void setUp() {
        lifecycleService = mock(QuestLifecycleService.class);
        questService = mock(QuestService.class);
        dispatcher = mock(EventDispatcher.class);
        questRegistry = new QuestRegistry();

        when(questService.saveQuest(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(lifecycleService.completeObjective(any(), any())).thenAnswer(
                invocation -> CompletableFuture.completedFuture(null));

        progressService = new QuestProgressService(lifecycleService, questService, dispatcher, questRegistry);

        groupConfig = new QuestGroupConfig.Builder("group", "Group", "desc", "player").build();
        questConfig = new QuestConfig("quest", "Quest", "desc", 0);
        groupConfig.addQuest(questConfig);
        actor = new TestActor(UUID.randomUUID());
    }

    /** Builds a registered quest carrying one objective, both identified by stable keys. */
    private TestObjective buildRegisteredQuest(UUID questId, String objectiveKey, int progress, int target) {
        Quest quest = new Quest(questId, questConfig, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());

        QuestStageConfig stageConfig = new QuestStageConfig("stage", "Stage", Flow.PARALLEL);
        QuestStage stage = new QuestStage(quest, stageConfig);
        quest.addStage(stage);

        TestObjective objective = new TestObjective(stage, objectiveKey, progress, target);
        stage.addObjective(objective);

        actor.addQuest(quest);
        questRegistry.add(quest);
        return objective;
    }

    @Test
    void batchedProgressLandsOnTheInstanceCurrentlyInTheRegistry() {
        UUID questId = UUID.randomUUID();
        TestObjective stale = buildRegisteredQuest(questId, "obj", 0, 10);

        progressService.progressObjective(stale, 3, null);

        // Reconnect: the whole quest graph is rebuilt and replaces the previous one.
        TestObjective reloaded = buildRegisteredQuest(questId, "obj", 0, 10);

        progressService.flushPendingProgress().join();

        assertEquals(3, reloaded.getProgress(), "pending progress must apply to the live objective");
        assertEquals(0, stale.getProgress(), "the detached instance must not be touched");
    }

    @Test
    void progressOnDistinctInstancesOfTheSameObjectiveAccumulatesOnce() {
        UUID questId = UUID.randomUUID();
        TestObjective first = buildRegisteredQuest(questId, "obj", 0, 10);
        progressService.progressObjective(first, 2, null);

        TestObjective second = buildRegisteredQuest(questId, "obj", 0, 10);
        progressService.progressObjective(second, 4, null);

        progressService.flushPendingProgress().join();

        assertEquals(6, second.getProgress(), "both batches share the (questId, objectiveKey) slot");
    }

    @Test
    void pendingProgressIsDroppedWhenTheQuestLeavesTheRegistry() {
        UUID questId = UUID.randomUUID();
        TestObjective objective = buildRegisteredQuest(questId, "obj", 0, 10);

        progressService.progressObjective(objective, 3, null);
        questRegistry.remove(objective.getQuest());

        assertDoesNotThrow(() -> progressService.flushPendingProgress().join());
        verify(questService, never()).saveQuest(any());
    }

    @Test
    void reachingTargetFlushesImmediatelyAndCompletesTheObjective() {
        UUID questId = UUID.randomUUID();
        TestObjective objective = buildRegisteredQuest(questId, "obj", 0, 5);

        progressService.progressObjective(objective, 5, null);

        assertEquals(5, objective.getProgress(), "hitting the target must flush without waiting for the timer");
        verify(lifecycleService).completeObjective(objective, null);
    }
}
