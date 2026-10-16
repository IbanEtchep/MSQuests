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
import net.artisanmc.modules.api.action.ActionContext;
import net.artisanmc.modules.api.action.ActionDeclaration;
import net.artisanmc.modules.api.action.ActionRuntime;
import net.artisanmc.modules.api.model.ModelField;
import org.bukkit.entity.Player;
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
import static org.mockito.Mockito.*;

class QuestActionsTest {

    private final UUID playerId = UUID.randomUUID();
    private final List<String> warnings = new ArrayList<>();

    private QuestLifecycleService lifecycle;
    private QuestConfigRegistry configs;
    private QuestGroupConfig daily;
    private QuestConfig mineStone;
    private TestActor actor;
    private Map<String, ActionDeclaration> actions;
    private ActionContext ctx;
    private ActionRuntime runtime;

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

        BukkitQuestsPlugin plugin = mock(BukkitQuestsPlugin.class);
        when(plugin.getQuestConfigRegistry()).thenReturn(configs);
        when(plugin.getQuestActorRegistry()).thenReturn(actors);
        when(plugin.getQuestLifecycleService()).thenReturn(lifecycle);

        actions = new HashMap<>();
        for (ActionDeclaration declaration : new QuestActions(plugin).declarations()) {
            actions.put(declaration.getName(), declaration);
        }

        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        ctx = context(player, Map.of("row_key", "mine_stone"));
        runtime = new ActionRuntime() {
            @Override
            public String text(ActionContext c, @Nullable Object text) {
                if (text == null) return "";
                String out = String.valueOf(text);
                for (Map.Entry<String, Object> arg : c.getArgs().entrySet()) {
                    out = out.replace("{" + arg.getKey() + "}", String.valueOf(arg.getValue()));
                }
                return out;
            }

            @Override
            public ActionContext run(ActionContext c, @Nullable Object chain) {
                return c;
            }

            @Override
            public void warn(String message) {
                warnings.add(message);
            }
        };
    }

    private ActionContext run(String name, Object wire) {
        ActionDeclaration declaration = actions.get(name);
        return declaration.getHandler().execute(ctx, declaration.bind(wire), runtime);
    }

    // ------------------------------------------------------------ declarations

    @Test
    void declaresTheQuestActionsWithAGroupPicker() {
        assertEquals(java.util.Set.of("quests:start", "quests:track", "quests:rotate", "quests:complete",
                "quests:distribute"), actions.keySet());
        for (ActionDeclaration declaration : actions.values()) {
            ModelField group = declaration.arg("group");
            assertNotNull(group, declaration.getName());
            assertTrue(group.isRequired());
            assertEquals("quest_group", group.getType().getDocumentKind(), declaration.getName());
        }
        assertTrue(actions.get("quests:start").arg("quest").isRequired());
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

        assertSame(ctx, run("quests:complete", Map.of("group", "daily", "quest", "mine_stone")));

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
        assertSame(ctx, run("quests:complete", Map.of("group", "weekly", "quest", "mine_stone")));
        assertSame(ctx, run("quests:complete", Map.of("group", "daily", "quest", "nope")));

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

    private static ActionContext context(Player player, Map<String, Object> args) {
        return new ActionContext() {
            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public Map<String, Object> getArgs() {
                return args;
            }

            @Override
            public String getLang() {
                return "fr";
            }

            @Override
            public @Nullable String getProjectId() {
                return "quests";
            }

            @Override
            public ActionContext withArgs(Map<String, Object> more) {
                Map<String, Object> merged = new HashMap<>(args);
                merged.putAll(more);
                return context(player, merged);
            }
        };
    }
}
