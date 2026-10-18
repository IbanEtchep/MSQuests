package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.suite.ActionArg;
import com.github.ibanetchep.msquests.suite.ActionCall;
import com.github.ibanetchep.msquests.suite.ActionSpec;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.bukkit.service.TrackingBossBarService;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.service.PlayerProfileService;
import com.tcoded.folialib.impl.PlatformScheduler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuestActionsTest {

    private final UUID playerId = UUID.randomUUID();
    private final List<String> warnings = new ArrayList<>();

    private QuestLifecycleService lifecycle;
    private QuestConfigRegistry configs;
    private QuestGroupConfig daily;
    private QuestConfig mineStone;
    private TestActor actor;
    private Map<String, ActionSpec> actions;
    private Map<String, Object> ctxArgs;
    private BukkitQuestsPlugin plugin;
    private Player player;

    @TempDir
    Path langFolder;

    @BeforeEach
    void setUp() {
        daily = ArtisanFixtures.group("daily", "player");
        mineStone = ArtisanFixtures.quest(daily, "mine_stone", "easy");

        configs = new QuestConfigRegistry();
        configs.registerQuestGroupConfig(daily);
        actor = new TestActor(playerId, "Steve", "player");
        QuestActorRegistry actors = new QuestActorRegistry();
        actors.registerActor(actor);
        lifecycle = mock(QuestLifecycleService.class);

        plugin = mock(BukkitQuestsPlugin.class);
        when(plugin.getQuestConfigRegistry()).thenReturn(configs);
        when(plugin.getQuestActorRegistry()).thenReturn(actors);
        when(plugin.getQuestLifecycleService()).thenReturn(lifecycle);

        actions = new HashMap<>();
        for (ActionSpec declaration : new QuestActions(plugin).declarations()) {
            actions.put(declaration.getName(), declaration);
        }

        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        ctxArgs = Map.of("row_key", "mine_stone");
    }

    private void run(String name, Object wire) {
        run(name, wire, ctxArgs);
    }

    /** The engine reduced to what the handlers use: `{name}` placeholders read the chain's bindings. */
    private void run(String name, Object wire, Map<String, Object> contextArgs) {
        ActionSpec declaration = actions.get(name);
        declaration.getHandler().run(new ActionCall() {
            @Override
            public @NotNull Player getPlayer() {
                return player;
            }

            @Override
            public @NotNull Map<String, Object> getArgs() {
                return declaration.bind(wire);
            }

            @Override
            public @NotNull Map<String, Object> getContextArgs() {
                return contextArgs;
            }

            @Override
            public @NotNull String text(@Nullable Object text) {
                if (text == null) return "";
                String out = String.valueOf(text);
                for (Map.Entry<String, Object> arg : contextArgs.entrySet()) {
                    out = out.replace("{" + arg.getKey() + "}", String.valueOf(arg.getValue()));
                }
                return out;
            }

            @Override
            public void warn(@NotNull String message) {
                warnings.add(message);
            }
        });
    }

    // ------------------------------------------------------------ declarations

    @Test
    void declaresTheQuestActionsWithAGroupPicker() {
        assertEquals(java.util.Set.of("quests:start", "quests:track", "quests:rotate", "quests:complete",
                "quests:distribute", "quests:abandon", "quests:action_bar", "quests:boss_bar"), actions.keySet());
        for (ActionSpec declaration : actions.values()) {
            if (declaration.getName().endsWith("_bar")) continue; // display actions, no group
            ActionArg group = declaration.arg("group");
            assertNotNull(group, declaration.getName());
            assertTrue(group.getRequired());
            assertEquals(ActionArg.Type.DOCUMENT_REF, group.getType(), declaration.getName());
            assertEquals("quest_group", group.getDocumentKind(), declaration.getName());
        }
        assertTrue(actions.get("quests:start").arg("quest").getRequired());
        assertEquals("group", actions.get("quests:distribute").getPrimary());
    }

    @Test
    void distributeBindsTheScalarWireToTheGroup() {
        assertEquals(Map.of("group", "daily"), actions.get("quests:distribute").bind("daily"));
    }

    // ---------------------------------------------------------------- handlers

    @Test
    void completeActsOnTheActorsActiveInstance() {
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 3);

        run("quests:complete", Map.of("group", "daily", "quest", "mine_stone"));

        verify(lifecycle).completeQuest(quest);
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void completeIgnoresAFinishedInstance() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.COMPLETED, 64);

        run("quests:complete", Map.of("group", "daily", "quest", "mine_stone"));

        verify(lifecycle, never()).completeQuest(any());
    }

    @Test
    void argumentsAreInterpolatedAndQualifiedRefsResolved() {
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 3);

        run("quests:complete", Map.of("group", "quests:daily", "quest", "{row_key}"));

        verify(lifecycle).completeQuest(quest);
    }

    @Test
    void distributeWithoutAmountUsesTheGroupSettings() {
        run("quests:distribute", "daily");

        verify(lifecycle).triggerDistribution(actor, daily);
        verify(lifecycle, never()).distributeQuests(any(), any(), any(), anyInt());
    }

    @Test
    void distributeWithAmountHandsOutThatMany() {
        run("quests:distribute", Map.of("group", "daily", "amount", 2));

        verify(lifecycle).distributeQuests(actor, daily, daily.getDistributionStrategy(), 2);
    }

    @Test
    void unknownGroupOrQuestWarnsAndKeepsTheChainGoing() {
        run("quests:complete", Map.of("group", "weekly", "quest", "mine_stone"));
        run("quests:complete", Map.of("group", "daily", "quest", "nope"));

        assertEquals(2, warnings.size(), warnings::toString);
        assertTrue(warnings.get(0).contains("weekly"));
        assertTrue(warnings.get(1).contains("nope"));
        verifyNoInteractions(lifecycle);
    }

    @Test
    void aPlayerWithoutAnActorForTheGroupIsANoOp() {
        QuestGroupConfig guildGroup = ArtisanFixtures.group("guild_daily", "guild");
        ArtisanFixtures.quest(guildGroup, "bank", "easy");
        configs.registerQuestGroupConfig(guildGroup);

        run("quests:distribute", "guild_daily");

        verifyNoInteractions(lifecycle);
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void groupKeyStripsAProjectQualifier() {
        assertEquals("daily", QuestActions.groupKey("daily"));
        assertEquals("daily", QuestActions.groupKey("quests:daily"));
        assertEquals("daily", QuestActions.groupKey(" daily "));
        assertNull(QuestActions.groupKey(""));
        assertNull(QuestActions.groupKey("quests:"));
        assertNull(QuestActions.groupKey(null));
    }

    // ------------------------------------------------------------ L3 actions

    @Test
    void abandonFailsTheActiveInstanceAndUntracksIt() {
        loadTranslator();
        Quest quest = ArtisanFixtures.instance(actor, mineStone, QuestStatus.IN_PROGRESS, 3);
        PlayerProfile profile = new PlayerProfile(playerId, "Steve");
        profile.setTrackedQuestId(quest.getId());
        PlayerProfileRegistry profiles = mock(PlayerProfileRegistry.class);
        when(profiles.getPlayerProfile(playerId)).thenReturn(profile);
        when(plugin.getPlayerProfileRegistry()).thenReturn(profiles);
        when(plugin.getPlayerProfileService()).thenReturn(mock(PlayerProfileService.class));
        when(plugin.getTrackingBossBarService()).thenReturn(mock(TrackingBossBarService.class));

        run("quests:abandon", Map.of("group", "daily", "quest", "mine_stone"));

        verify(lifecycle).abandonQuest(quest);
        assertNull(profile.getTrackedQuestId());
        verify(player).sendMessage(any(Component.class));
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void abandonIgnoresAFinishedInstance() {
        ArtisanFixtures.instance(actor, mineStone, QuestStatus.COMPLETED, 64);

        run("quests:abandon", Map.of("group", "daily", "quest", "mine_stone"));

        verify(lifecycle, never()).abandonQuest(any());
    }

    @Test
    void actionBarSendsTheInterpolatedText() {
        run("quests:action_bar", "<green>{row_key}");

        verify(player).sendActionBar(argThat((Component c) ->
                "mine_stone".equals(PlainTextComponentSerializer.plainText().serialize(c))));
    }

    @Test
    void bossBarShowsTheChainProgress() {
        PlatformScheduler scheduler = scheduler();
        Map<String, Object> objectiveHook = new HashMap<>(ctxArgs);
        objectiveHook.put("progress_ratio", 0.25);

        run("quests:boss_bar", Map.of("text", "{row_key}", "color", "red"), objectiveHook);

        ArgumentCaptor<BossBar> bar = ArgumentCaptor.forClass(BossBar.class);
        verify(player).showBossBar(bar.capture());
        assertEquals(0.25f, bar.getValue().progress(), 1e-6);
        assertEquals(BossBar.Color.RED, bar.getValue().color());
        assertEquals(BossBar.Overlay.PROGRESS, bar.getValue().overlay());
        assertEquals("mine_stone", PlainTextComponentSerializer.plainText().serialize(bar.getValue().name()));
        verify(scheduler).runLater(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    void bossBarWarnsOnAnUnknownColor() {
        scheduler();

        run("quests:boss_bar", Map.of("text", "x", "color", "teal"));

        assertEquals(1, warnings.size(), warnings::toString);
        verify(player).showBossBar(argThat((BossBar b) -> b.color() == BossBar.Color.WHITE));
    }

    private PlatformScheduler scheduler() {
        PlatformScheduler scheduler = mock(PlatformScheduler.class);
        when(scheduler.runLater(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mock(com.tcoded.folialib.wrapper.task.WrappedTask.class));
        when(plugin.getScheduler()).thenReturn(scheduler);
        return scheduler;
    }

    private void loadTranslator() {
        new BukkitTranslator(langFolder.toFile(), "fr_FR",
                locale -> getClass().getResourceAsStream("/lang/" + locale + ".yml"),
                java.util.logging.Logger.getAnonymousLogger()).load();
    }
}
