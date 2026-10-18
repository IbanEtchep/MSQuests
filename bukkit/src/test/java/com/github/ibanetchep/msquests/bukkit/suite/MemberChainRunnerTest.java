package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.bukkit.artisan.EditorProject;
import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainContext;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Chains touch the Bukkit API (online members), but the lifecycle runs some hooks from
 * virtual threads: the runner must hop to the server thread, then run the chain once per
 * online member with the quest bindings.
 */
class MemberChainRunnerTest {

    private ServerMock server;
    private BukkitQuestsPlugin plugin;
    private ChainExecutor actions;
    private MemberChainRunner runner;
    private final ActionChain chain = ActionChain.of(List.of(Map.of("send_message", "{quest_title}")));

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = mock(BukkitQuestsPlugin.class);
        when(plugin.getTranslator()).thenReturn(mock(BukkitTranslator.class));
        actions = mock(ChainExecutor.class);
        runner = new MemberChainRunner(plugin, actions);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Quest questOf(UUID... memberIds) {
        QuestGroupConfig group = ArtisanFixtures.group("daily", "player");
        QuestConfig config = ArtisanFixtures.quest(group, "mine_stone", "easy");
        TestActor actor = new TestActor(UUID.randomUUID(), "Actor", "player");
        for (UUID id : memberIds) actor.addProfile(new PlayerProfile(id, "p"));
        return ArtisanFixtures.instance(actor, config, QuestStatus.IN_PROGRESS, 3);
    }

    @SuppressWarnings("unchecked")
    @Test
    void runsOncePerOnlineMemberWithQuestBindings() {
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId(), UUID.randomUUID());
        quest.getActor().getProfiles().stream()
                .filter(p -> p.getId().equals(online.getUniqueId()))
                .forEach(p -> p.setTrackedQuestId(quest.getId()));

        runner.run(chain, ChainContext.of(quest));

        ArgumentCaptor<Map<String, Object>> args = ArgumentCaptor.forClass(Map.class);
        verify(actions, times(1)).run(eq(online), eq(chain.steps()), args.capture(), eq(EditorProject.PROJECT_ID));
        assertEquals("mine_stone", args.getValue().get("quest"));
        assertEquals("Quest mine_stone", args.getValue().get("quest_title"));
        assertEquals("daily", args.getValue().get("group"));
        assertEquals(true, args.getValue().get("tracked"));
        assertTrue(args.getValue().containsKey("objectives"));
        verify(plugin, never()).runSync(any());
    }

    @Test
    void emptyChainRunsNothing() {
        Player online = server.addPlayer();
        runner.run(ActionChain.EMPTY, ChainContext.of(questOf(online.getUniqueId())));
        verifyNoInteractions(actions);
    }

    @Test
    void deferredToTheMainThreadWhenCalledOffThread() throws InterruptedException {
        Player online = server.addPlayer();
        Quest quest = questOf(online.getUniqueId());

        Thread offMain = new Thread(() -> runner.run(chain, ChainContext.of(quest)));
        offMain.start();
        offMain.join();

        verifyNoInteractions(actions);
        ArgumentCaptor<Runnable> scheduled = ArgumentCaptor.forClass(Runnable.class);
        verify(plugin).runSync(scheduled.capture());

        scheduled.getValue().run();
        verify(actions).run(eq(online), eq(chain.steps()), anyMap(), eq(EditorProject.PROJECT_ID));
    }

    @Test
    void runsInTheProjectHoldingTheGroupsDocument() {
        Player online = server.addPlayer();
        QuestGroupConfig group = new QuestGroupConfig.Builder("levels", "Levels", "", "player")
                .sourceProject("guildes")
                .build();
        TestActor actor = new TestActor(UUID.randomUUID(), "Actor", "player");
        actor.addProfile(new PlayerProfile(online.getUniqueId(), "p"));

        runner.run(chain, ChainContext.of(actor, group));

        verify(actions).run(eq(online), eq(chain.steps()), anyMap(), eq("guildes"));
    }
}
