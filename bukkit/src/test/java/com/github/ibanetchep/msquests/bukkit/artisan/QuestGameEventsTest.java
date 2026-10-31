package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.core.event.CoreQuestCompletedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestExpiredEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveCompletedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestObjectiveProgressedEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestStageCompletedEvent;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import net.artisanmc.modules.api.gameevent.GameEventDeclaration;
import net.artisanmc.modules.api.gameevent.GameEventFrame;
import net.artisanmc.modules.api.gameevent.GameEventRegistry;
import net.artisanmc.modules.api.scheduler.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The quests' Game events fire once per online member of the quest's actor, on that member's
 * thread, with the keys an author writes; nothing is done while no Trigger listens.
 */
class QuestGameEventsTest {

    private ServerMock server;
    private GameEventRegistry registry;
    private final List<GameEventFrame> fired = new ArrayList<>();
    private final List<Runnable> serverThread = new ArrayList<>();
    private final List<Runnable> playerThreads = new ArrayList<>();
    private boolean onServerThread;
    private QuestGameEvents events;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        registry = new GameEventRegistry();
        QuestGameEvents.declarations().forEach(registry::register);
        TaskScheduler scheduler = new RecordingScheduler();
        events = new QuestGameEvents(registry, scheduler, server::getPlayer,
                () -> onServerThread, serverThread::add);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void listen() {
        registry.addListener(fired::add);
    }

    private Quest questOf(UUID... memberIds) {
        QuestGroupConfig group = ArtisanFixtures.group("daily", "player");
        QuestConfig config = ArtisanFixtures.quest(group, "miner", "easy");
        TestActor actor = new TestActor(UUID.randomUUID(), "Actor", "player");
        for (UUID id : memberIds) actor.addProfile(new PlayerProfile(id, "p"));
        return ArtisanFixtures.instance(actor, config, QuestStatus.IN_PROGRESS, 0);
    }

    private static QuestObjective objectiveOf(Quest quest) {
        return quest.getStages().values().iterator().next().getObjectiveList().get(0);
    }

    private void runAll() {
        new ArrayList<>(serverThread).forEach(Runnable::run);
        serverThread.clear();
        new ArrayList<>(playerThreads).forEach(Runnable::run);
        playerThreads.clear();
    }

    @Test
    void declaresSixNamespacedEventsTheRegistryAccepts() {
        assertEquals(List.of(
                        QuestGameEvents.STARTED, QuestGameEvents.OBJECTIVE_COMPLETED, QuestGameEvents.STAGE_COMPLETED,
                        QuestGameEvents.COMPLETED, QuestGameEvents.EXPIRED, QuestGameEvents.ABANDONED),
                QuestGameEvents.declarations().stream().map(GameEventDeclaration::getName).toList());
        GameEventDeclaration objective = registry.find(QuestGameEvents.OBJECTIVE_COMPLETED);
        assertNotNull(objective);
        assertEquals(List.of("group", "quest", "actor_type", "stage", "objective", "objective_type"),
                objective.getContext().stream().map(f -> f.getName()).toList());
    }

    @Test
    void nothingIsScheduledWhileNoListenerObserves() {
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId());

        events.accept(new CoreQuestCompletedEvent(quest));

        assertTrue(serverThread.isEmpty());
        assertTrue(playerThreads.isEmpty());
    }

    @Test
    void firesOncePerOnlineMemberOnTheirThreadWithTheObjectiveKeys() {
        listen();
        Player first = server.addPlayer();
        Player second = server.addPlayer();
        Quest quest = questOf(first.getUniqueId(), second.getUniqueId(), UUID.randomUUID());

        events.accept(new CoreQuestObjectiveCompletedEvent(objectiveOf(quest), null));
        assertTrue(fired.isEmpty(), "off the server thread: members are read on the next tick");
        assertEquals(1, serverThread.size());

        runAll();

        assertEquals(java.util.Set.of(first, second), java.util.Set.copyOf(fired.stream().map(GameEventFrame::getPlayer).toList()));
        assertEquals(2, fired.size());
        Map<String, ?> context = fired.get(0).getContext();
        assertEquals("daily", context.get("group"));
        assertEquals("miner", context.get("quest"));
        assertEquals("player", context.get("actor_type"));
        assertEquals("stage_1", context.get("stage"));
        assertEquals("obj_1", context.get("objective"));
        assertEquals("block_break", context.get("objective_type"));
    }

    @Test
    void stageAndEndEventsCarryTheirName() {
        listen();
        onServerThread = true;
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId());

        events.accept(new CoreQuestStageCompletedEvent(objectiveOf(quest).getStage(), null));
        events.accept(new CoreQuestExpiredEvent(quest));
        runAll();

        assertEquals(List.of(QuestGameEvents.STAGE_COMPLETED, QuestGameEvents.EXPIRED),
                fired.stream().map(f -> f.getDeclaration().getName()).toList());
        assertEquals("stage_1", fired.get(0).getContext().get("stage"));
        assertFalse(fired.get(1).getContext().containsKey("stage"));
    }

    @Test
    void aPlayerWhoLeftBeforeTheirThreadRanGetsNothing() {
        listen();
        onServerThread = true;
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId());

        events.accept(new CoreQuestCompletedEvent(quest));
        ((org.mockbukkit.mockbukkit.entity.PlayerMock) online).disconnect();
        runAll();

        assertTrue(fired.isEmpty());
    }

    @Test
    void otherCoreEventsAreIgnored() {
        listen();
        onServerThread = true;
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId());

        events.accept(new CoreQuestObjectiveProgressedEvent(objectiveOf(quest), null));
        runAll();

        assertTrue(fired.isEmpty());
    }

    /** No thread owns a player here: every member's task is queued, run by {@link #runAll}. */
    private final class RecordingScheduler implements TaskScheduler {
        @Override public void runGlobal(Runnable task) { serverThread.add(task); }
        @Override public void runGlobalLater(long delayTicks, Runnable task) { serverThread.add(task); }
        @Override public void runAt(Location location, Runnable task) { playerThreads.add(task); }
        @Override public void runAtLater(Location location, long delayTicks, Runnable task) { playerThreads.add(task); }
        @Override public void runFor(Entity entity, Runnable task) { playerThreads.add(task); }
        @Override public void runForLater(Entity entity, long delayTicks, Runnable task) { playerThreads.add(task); }
        @Override public void runAsync(Runnable task) { task.run(); }
        @Override public void runAsyncLater(long delayTicks, Runnable task) { task.run(); }
        @Override public boolean isOwnedByCurrentThread(Entity entity) { return false; }
        @Override public boolean isGlobalThread() { return onServerThread; }
    }
}
