package com.github.ibanetchep.msquests.bukkit.quest.action;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Quest actions touch the Bukkit API, but the lifecycle drives some of them from
 * {@code AtomicLocalQuestExecutor}'s virtual threads. The base class must be the thing
 * that guarantees main-thread execution, so a new action cannot forget to.
 */
class BukkitQuestActionThreadingTest {

    private BukkitQuestsPlugin plugin;
    private RecordingAction action;

    static class RecordingAction extends BukkitQuestAction {
        final AtomicInteger runs = new AtomicInteger();

        RecordingAction(BukkitQuestsPlugin plugin) {
            super(new QuestActionDTO("test", null, Map.of(), null), plugin);
        }

        @Override
        protected void perform(Quest quest) {
            runs.incrementAndGet();
        }

        @Override
        public QuestActionDTO toDTO() {
            return new QuestActionDTO("test", null, Map.of(), null);
        }
    }

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = mock(BukkitQuestsPlugin.class);
        action = new RecordingAction(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void runsInlineWhenAlreadyOnTheMainThread() {
        action.execute(mock(Quest.class));

        assertEquals(1, action.runs.get());
        verify(plugin, never()).runSync(any());
    }

    @Test
    void deferredToTheMainThreadWhenCalledOffThread() throws InterruptedException {
        Quest quest = mock(Quest.class);

        Thread offMain = new Thread(() -> action.execute(quest));
        offMain.start();
        offMain.join();

        assertEquals(0, action.runs.get(), "must not touch the Bukkit API off the main thread");

        ArgumentCaptor<Runnable> scheduled = ArgumentCaptor.forClass(Runnable.class);
        verify(plugin).runSync(scheduled.capture());

        scheduled.getValue().run();
        assertEquals(1, action.runs.get());
    }

    @Test
    void objectiveOverloadIsAlsoDeferred() throws InterruptedException {
        QuestObjective objective = mock(QuestObjective.class);
        when(objective.getQuest()).thenReturn(mock(Quest.class));

        Thread offMain = new Thread(() -> action.execute(objective));
        offMain.start();
        offMain.join();

        assertEquals(0, action.runs.get());
        verify(plugin).runSync(any());
    }

    @Test
    void actorOverloadIsAlsoDeferred() throws InterruptedException {
        QuestActor actor = mock(QuestActor.class);
        QuestGroupConfig groupConfig = new QuestGroupConfig.Builder("group", "Group", "desc", "player").build();

        Thread offMain = new Thread(() -> action.execute(actor, groupConfig));
        offMain.start();
        offMain.join();

        verify(plugin).runSync(any());
    }
}
