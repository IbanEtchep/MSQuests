package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestLifecycleAllCompleteTest {

    // -------------------------------------------------------------------------
    // Minimal concrete QuestObjectiveConfig for tests (core-only, no Bukkit)
    // -------------------------------------------------------------------------
    static class TestObjectiveConfig extends QuestObjectiveConfig {
        TestObjectiveConfig(String key) {
            super(new QuestObjectiveConfigDTO(key, "test", Map.of()));
        }

        @Override
        public QuestObjectiveConfigDTO toDTO() {
            return new QuestObjectiveConfigDTO(key, type, Map.of());
        }

        @Override
        public Map<String, String> getPlaceholders(com.github.ibanetchep.msquests.core.lang.Translator translator) {
            return Map.of();
        }
    }

    // -------------------------------------------------------------------------
    // Minimal concrete AbstractQuestObjective for tests
    // -------------------------------------------------------------------------
    static class TestObjective extends AbstractQuestObjective<TestObjectiveConfig> {
        TestObjective(QuestStage stage, String key, int progress, int target, QuestObjectiveStatus status) {
            super(stage, new TestObjectiveConfig(key), progress, target, status);
        }
    }

    // -------------------------------------------------------------------------
    // Minimal concrete QuestActor for tests
    // -------------------------------------------------------------------------
    static class TestActor extends QuestActor {
        private static final String TYPE = "player";

        TestActor() {
            super(UUID.randomUUID(), "TestActor");
        }

        @Override
        public String getActorType() {
            return TYPE;
        }

        @Override
        public boolean isMember(UUID playerId) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private QuestLifecycleService lifecycleService;
    private EventDispatcher dispatcher;
    private AtomicQuestExecutor executor;
    private QuestService persistenceService;
    private QuestFactory questFactory;
    private QuestRegistry questRegistry;
    private QuestConfigRegistry questConfigRegistry;
    private QuestDistributionService distributionService;
    private RotationRepository rotationRepository;

    @BeforeEach
    void setUp() {
        dispatcher = mock(EventDispatcher.class);
        executor = mock(AtomicQuestExecutor.class);
        persistenceService = mock(QuestService.class);
        questFactory = mock(QuestFactory.class);
        questRegistry = mock(QuestRegistry.class);
        questConfigRegistry = mock(QuestConfigRegistry.class);
        distributionService = mock(QuestDistributionService.class);
        rotationRepository = mock(RotationRepository.class);

        when(persistenceService.saveQuest(any())).thenReturn(CompletableFuture.completedFuture(null));

        lifecycleService = new QuestLifecycleService(
                dispatcher,
                persistenceService,
                questFactory,
                questRegistry,
                questConfigRegistry,
                executor,
                distributionService,
                rotationRepository
        );
    }

    // -------------------------------------------------------------------------
    // Helper: create a QuestGroupConfig with the allPeriodQuestsCompleteActions list
    // -------------------------------------------------------------------------
    private QuestGroupConfig buildGroupConfig(List<QuestAction> allCompleteActions) {
        return new QuestGroupConfig.Builder("group1", "Group 1", "desc", "player")
                .allPeriodQuestsCompleteActions(allCompleteActions)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helper: create a Quest whose single objective has progress == target (completes on complete())
    // -------------------------------------------------------------------------
    private Quest buildQuestWithSingleObjective(QuestActor actor, QuestGroupConfig groupConfig, String questKey, QuestObjectiveStatus initialStatus) {
        QuestConfig questConfig = new QuestConfig(questKey, questKey, "", 0);
        groupConfig.addQuest(questConfig);

        Quest quest = new Quest(UUID.randomUUID(), questConfig, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());

        QuestStageConfig stageConfig = new QuestStageConfig("stage1", "Stage 1", Flow.PARALLEL);
        QuestStage stage = new QuestStage(quest, stageConfig);
        quest.addStage(stage);

        TestObjective objective = new TestObjective(stage, "obj1", 0, 1, initialStatus);
        stage.addObjective(objective);

        actor.addQuest(quest);
        return quest;
    }

    // -------------------------------------------------------------------------
    // Helper: create an already-COMPLETED quest (no pending objectives)
    // -------------------------------------------------------------------------
    private Quest buildCompletedQuest(QuestActor actor, QuestGroupConfig groupConfig, String questKey) {
        QuestConfig questConfig = new QuestConfig(questKey, questKey, "", 0);
        groupConfig.addQuest(questConfig);

        Quest quest = new Quest(UUID.randomUUID(), questConfig, actor, QuestStatus.COMPLETED, new Date(), new Date(), new Date());

        // Add a completed stage/objective so shouldComplete() stays false
        QuestStageConfig stageConfig = new QuestStageConfig("stage1", "Stage 1", Flow.PARALLEL);
        QuestStage stage = new QuestStage(quest, stageConfig);
        quest.addStage(stage);

        TestObjective objective = new TestObjective(stage, "obj1", 1, 1, QuestObjectiveStatus.COMPLETED);
        stage.addObjective(objective);

        actor.addQuest(quest);
        return quest;
    }

    // -------------------------------------------------------------------------
    // Helper: wire executor to call consumer immediately with the given quest
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void wireExecutor(Quest quest) {
        when(executor.execute(eq(quest.getId()), any())).thenAnswer(invocation -> {
            Consumer<Quest> consumer = invocation.getArgument(1);
            consumer.accept(quest);
            return CompletableFuture.completedFuture(quest);
        });
    }

    // =========================================================================
    // Test 1: allPeriodQuestsCompleteAction fired when last quest completes
    // =========================================================================
    @Test
    void allPeriodQuestsCompleteActionFiredWhenLastQuestCompletes() {
        QuestAction allCompleteAction = mock(QuestAction.class);
        QuestGroupConfig groupConfig = buildGroupConfig(List.of(allCompleteAction));

        TestActor actor = new TestActor();

        // Quest 1: already completed
        buildCompletedQuest(actor, groupConfig, "quest1");

        // Quest 2: in-progress, with one objective that, when completed, will complete the quest
        Quest quest2 = buildQuestWithSingleObjective(actor, groupConfig, "quest2", QuestObjectiveStatus.IN_PROGRESS);
        TestObjective objective2 = (TestObjective) quest2.getStages().get("stage1").getObjectives().get("obj1");

        wireExecutor(quest2);

        lifecycleService.completeObjective(objective2, null).join();

        verify(allCompleteAction, times(1)).execute(actor, groupConfig);
    }

    // =========================================================================
    // Test 2: allPeriodQuestsCompleteAction NOT fired when quests still in progress
    // =========================================================================
    @Test
    void allPeriodQuestsCompleteActionNotFiredWhenQuestsStillInProgress() {
        QuestAction allCompleteAction = mock(QuestAction.class);
        QuestGroupConfig groupConfig = buildGroupConfig(List.of(allCompleteAction));

        TestActor actor = new TestActor();

        // Quest 1: in-progress with remaining (not yet complete) objectives
        Quest quest1 = buildQuestWithSingleObjective(actor, groupConfig, "quest1", QuestObjectiveStatus.IN_PROGRESS);

        // Quest 2: in-progress, with one objective that will complete when called
        Quest quest2 = buildQuestWithSingleObjective(actor, groupConfig, "quest2", QuestObjectiveStatus.IN_PROGRESS);
        TestObjective objective2 = (TestObjective) quest2.getStages().get("stage1").getObjectives().get("obj1");

        wireExecutor(quest2);

        lifecycleService.completeObjective(objective2, null).join();

        // Quest 1 is still in progress -> allPeriodQuestsCompleteAction must NOT fire
        verify(allCompleteAction, never()).execute(actor, groupConfig);
    }

    // =========================================================================
    // Test 3: No error when allPeriodQuestsCompleteActions is empty
    // =========================================================================
    @Test
    void noErrorWhenNoAllCompleteActionsConfigured() {
        QuestGroupConfig groupConfig = buildGroupConfig(List.of());

        TestActor actor = new TestActor();

        Quest quest = buildQuestWithSingleObjective(actor, groupConfig, "quest1", QuestObjectiveStatus.IN_PROGRESS);
        TestObjective objective = (TestObjective) quest.getStages().get("stage1").getObjectives().get("obj1");

        wireExecutor(quest);

        assertDoesNotThrow(() -> lifecycleService.completeObjective(objective, null).join());
    }
}
