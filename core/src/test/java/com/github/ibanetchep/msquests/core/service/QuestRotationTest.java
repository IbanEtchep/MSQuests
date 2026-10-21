package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A rotation is a click on the player's thread: the old quest's row is deleted in the
 * background and the click never waits on the connection pool.
 */
class QuestRotationTest {

    static class TestActor extends QuestActor {
        TestActor() { super(UUID.randomUUID(), "Steve"); }
        @Override public String getActorType() { return "player"; }
        @Override public boolean isMember(UUID playerId) { return false; }
    }

    @Test
    void theOldRowIsDeletedWithoutWaitingForTheDatabase() {
        QuestGroupConfig group = new QuestGroupConfig.Builder("daily", "Daily", "", "player")
                .maxActive(6).rotatable(true).build();
        QuestConfig mine = new QuestConfig("mine", "Mine", "", 0L);
        QuestConfig fish = new QuestConfig("fish", "Fish", "", 0L);
        group.addQuest(mine);
        group.addQuest(fish);

        TestActor actor = new TestActor();
        Quest current = new Quest(UUID.randomUUID(), mine, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
        actor.addQuest(current);

        QuestService persistence = mock(QuestService.class);
        // A pool with no free connection: the delete only completes much later.
        when(persistence.deleteQuest(any())).thenReturn(new CompletableFuture<>());
        when(persistence.saveQuest(any())).thenReturn(CompletableFuture.completedFuture(null));
        QuestDistributionService distribution = mock(QuestDistributionService.class);
        when(distribution.canStartQuest(any(), any(), any(), any())).thenReturn(QuestStartResult.SUCCESS);
        QuestFactory factory = mock(QuestFactory.class);
        when(factory.createQuest(eq(fish), eq(actor))).thenAnswer(invocation -> {
            // The real factory attaches the quest to its actor.
            Quest started = new Quest(UUID.randomUUID(), fish, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
            actor.addQuest(started);
            return started;
        });
        RotationRepository rotations = mock(RotationRepository.class);
        when(rotations.save(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        QuestRegistry registry = mock(QuestRegistry.class);

        QuestLifecycleService service = new QuestLifecycleService(
                mock(EventDispatcher.class), persistence, factory, registry, mock(QuestConfigRegistry.class),
                mock(AtomicQuestExecutor.class), distribution, rotations, mock(MSQuestsPlatform.class));

        QuestRotateResult result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> service.rotateQuest(actor, current));

        assertEquals(QuestRotateResult.SUCCESS, result);
        verify(persistence).deleteQuest(current);
        verify(registry).remove(current);
        assertFalse(actor.getQuests().containsKey(current.getId()), "the old quest left memory at once");
        assertTrue(actor.getActorQuestGroup(group).hasActive("fish"), "the alternative started");
        assertEquals(1, actor.getActorQuestGroup(group).getRotationsInPeriod());
    }
}
