package com.github.ibanetchep.msquests.bukkit.suite;

import com.github.ibanetchep.msquests.bukkit.BukkitQuestsPlugin;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures;
import com.github.ibanetchep.msquests.bukkit.artisan.ArtisanFixtures.TestActor;
import com.github.ibanetchep.msquests.bukkit.config.ContentSource;
import com.github.ibanetchep.msquests.bukkit.config.GlobalConfig;
import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.ActionChain;
import com.github.ibanetchep.msquests.core.quest.config.action.ChainContext;
import com.github.ibanetchep.msquests.core.quest.config.condition.Conditions;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The server without the Artisan plugin: the quests run on MSQuests' own copy of the runtime,
 * through the same ports as with Artisan. A reward chain with a requirement completes end to end.
 */
class LocalSuiteRuntimeTest {

    private ServerMock server;
    private BukkitQuestsPlugin plugin;
    private PlayerMock player;
    private SuiteRuntime suite;

    @TempDir
    Path dataFolder;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        player = server.addPlayer();

        plugin = mock(BukkitQuestsPlugin.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("LocalSuiteRuntimeTest"));
        when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        when(plugin.getGlobalConfig()).thenReturn(new GlobalConfig("fr_FR", ContentSource.ARTISAN, null, null, false));
        when(plugin.getQuestActorRegistry()).thenReturn(new QuestActorRegistry());
        when(plugin.getTranslator()).thenReturn(mock(BukkitTranslator.class));
        when(plugin.langText("artisan.menus_unavailable", "This interface needs the Artisan plugin.")).thenReturn("no menus");

        // No plugins/Artisan/projects: the plugin's own projects folder, with the quests project.
        Path quests = dataFolder.resolve("projects/quests");
        Files.createDirectories(quests.resolve("quest_groups"));
        Files.createDirectories(quests.resolve("lang"));
        Files.writeString(quests.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: fr\nlanguages: [fr]\n");
        Files.writeString(quests.resolve("lang/fr.yaml"), "reward:\n  done: \"Bravo {quest_title}\"\n");
        Files.writeString(quests.resolve("quest_groups/daily.yaml"), "schema: quest_group/v1\n");

        suite = LocalSuiteRuntime.start(plugin);
    }

    @AfterEach
    void tearDown() {
        if (suite != null) suite.close();
        MockBukkit.unmock();
    }

    @Test
    void runsWithoutArtisanOnTheOwnProjectsFolder() {
        assertFalse(suite.hasArtisan());
        assertNull(suite.translations());
        assertEquals(dataFolder.resolve("projects/quests").toFile(), suite.questsProject());
        assertEquals(List.of("quests"), suite.projectFiles().projectIds());
        assertEquals(List.of("daily.yaml"), suite.projectFiles().list("quests", "quest_groups"));
    }

    @Test
    void aRewardChainRunsForTheOnlineMemberInTheGroupsProject() {
        QuestGroupConfig group = ArtisanFixtures.group("daily", "player");
        QuestConfig config = ArtisanFixtures.quest(group, "mine_stone", "easy");
        TestActor actor = new TestActor(UUID.randomUUID(), "Actor", "player");
        actor.addProfile(new PlayerProfile(player.getUniqueId(), player.getName()));
        actor.addProfile(new PlayerProfile(UUID.randomUUID(), "offline"));
        Quest quest = ArtisanFixtures.instance(actor, config, QuestStatus.IN_PROGRESS, 3);
        ActionChain rewards = ActionChain.of(List.of(
                Map.of("give_item", Map.of("material", "IRON_INGOT", "count", 8)),
                Map.of("send_message", "{i18n:reward.done}")));

        suite.chainRunner().run(rewards, ChainContext.of(quest));

        assertTrue(player.getInventory().contains(Material.IRON_INGOT, 8));
        player.assertSaid("Bravo Quest mine_stone");
        player.assertNoMoreSaid();
    }

    @Test
    void requirementsAreCheckedOnThePlayerAndValidated() {
        PlayerProfile profile = new PlayerProfile(player.getUniqueId(), player.getName());
        Conditions vip = Conditions.of(List.of(Map.of("has_permission", "quests.vip")));

        assertFalse(suite.conditionChecker().test(vip, profile, Map.of()));
        player.addAttachment(MockBukkit.createMockPlugin(), "quests.vip", true);
        assertTrue(suite.conditionChecker().test(vip, profile, Map.of()));
        assertEquals(List.of(), suite.conditionChecker().validate(vip));
        assertEquals(1, suite.conditionChecker().validate(Conditions.of(List.of(Map.of("has_guild", "x")))).size());
        assertFalse(suite.conditionChecker().test(vip, new PlayerProfile(UUID.randomUUID(), "offline"), Map.of()));
    }

    @Test
    void itemsAreBuiltAndComparedByTheLocalRuntime() {
        Map<String, Object> spec = Map.of("material", "STONE");
        assertEquals(Material.STONE, suite.items().build(spec).getType());
        assertTrue(suite.items().matches(spec, new ItemStack(Material.STONE), Set.of()));
        assertFalse(suite.items().matches(spec, new ItemStack(Material.DIRT), Set.of("material")));
    }

    @Test
    void anExistingArtisanProjectsFolderIsReadInstead() throws Exception {
        File artisanProjects = new File(server.getPluginsFolder(), "Artisan/projects/shop");
        assertTrue(artisanProjects.mkdirs());
        try {
            SuiteRuntime other = LocalSuiteRuntime.start(plugin);
            assertEquals(new File(server.getPluginsFolder(), "Artisan/projects/quests"), other.questsProject());
            assertEquals(List.of("shop"), other.projectFiles().projectIds());
            other.close();
        } finally {
            Files.deleteIfExists(artisanProjects.toPath());
        }
    }
}
