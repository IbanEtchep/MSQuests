package com.github.ibanetchep.msquests.suite

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import java.io.File
import java.nio.file.Files
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The runtime MSQuests runs on without the Artisan plugin: Artisan's engine from MSQuests' own copy. */
class LocalRuntimeTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock
    private lateinit var player: PlayerMock
    private val projects = Files.createTempDirectory("msquests_projects_").toFile()
    private val calls = mutableListOf<ActionCall>()

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin("MSQuests")
        player = server.addPlayer()
        val quests = File(projects, "quests")
        File(quests, "quest_groups").mkdirs()
        File(quests, "lang").mkdirs()
        File(quests, ".artisan").mkdirs()
        File(quests, "manifest.yaml").writeText("schema: manifest/v1\ndefault_lang: fr\nlanguages: [fr, en]\n")
        File(quests, "lang/fr.yaml").writeText("reward:\n  done: \"Bravo {quest}\"\n")
        File(quests, "lang/en.yaml").writeText("reward:\n  done: \"Well done {quest}\"\n")
        File(quests, "quest_groups/daily.yaml").writeText("schema: quest_group/v1\n")
        File(quests, ".artisan/project-id").writeText("secret")
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
        projects.deleteRecursively()
    }

    private fun runtime(language: String = "fr_FR") = LocalRuntime(
        plugin, projects,
        listOf(ActionSpec("quests:test", listOf(ActionArg.ltext("text").required()), "text") { calls += it }),
        language, { "no menus here" }, Logger.getLogger("LocalRuntimeTest"),
    )

    private fun messages(): List<String> = generateSequence { player.nextMessage() }.toList()

    @Test
    fun `a reward chain gives the item and sends the message in the project's language`() {
        runtime().runChain(
            player,
            listOf(mapOf("give_item" to mapOf("material" to "IRON_INGOT", "count" to 8)), mapOf("send_message" to "{i18n:reward.done}")),
            mapOf("quest" to "miner"),
            "quests",
        )
        assertTrue(player.inventory.contains(Material.IRON_INGOT, 8))
        assertEquals(listOf("Bravo miner"), messages())
    }

    @Test
    fun `the plugin's language picks the project's translation, the default one is the fallback`() {
        runtime("en_US").runChain(player, listOf(mapOf("send_message" to "{i18n:reward.done}")), mapOf("quest" to "miner"), "quests")
        assertEquals(listOf("Well done miner"), messages())
    }

    @Test
    fun `the quests' own actions run from the local registry`() {
        runtime().runChain(player, listOf(mapOf("quests:test" to "hello {quest}")), mapOf("quest" to "miner"), "quests")
        assertEquals(1, calls.size)
        assertEquals(player, calls[0].player)
        assertEquals("hello miner", calls[0].text(calls[0].args["text"]))
        assertEquals("miner", calls[0].contextArgs["quest"])
    }

    @Test
    fun `an unreadable chain and an unknown action are skipped, the chain goes on`() {
        runtime().runChain(player, "not a chain {", emptyMap(), null)
        runtime().runChain(player, listOf(mapOf("guilds:deposit" to 5), mapOf("send_message" to "after")), emptyMap(), null)
        assertEquals(listOf("after"), messages())
    }

    @Test
    fun `a menu in a chain tells the player the interface is unavailable`() {
        runtime().runChain(player, listOf(mapOf("open_menu" to "shop")), emptyMap(), "quests")
        assertEquals(listOf("no menus here"), messages())
    }

    @Test
    fun `conditions are evaluated for the player and validated strictly`() {
        val runtime = runtime()
        val permission = listOf(mapOf("has_permission" to "quests.vip"))
        assertFalse(runtime.testConditions(player, permission, emptyMap<String, Any>()))
        player.addAttachment(plugin, "quests.vip", true)
        assertTrue(runtime.testConditions(player, permission, emptyMap<String, Any>()))
        assertEquals(emptyList(), runtime.validateConditions(permission))
        assertEquals(1, runtime.validateConditions(listOf(mapOf("has_guild" to "x"))).size)
        assertFalse(runtime.testConditions(player, listOf(mapOf("has_guild" to "x")), emptyMap<String, Any>()))
    }

    @Test
    fun `item specs are built and compared`() {
        val runtime = runtime()
        val spec = mapOf("material" to "STONE")
        assertEquals(Material.STONE, runtime.buildItem(spec)?.type)
        assertTrue(runtime.matchesItem(spec, ItemStack(Material.STONE, 3), emptySet()))
        assertFalse(runtime.matchesItem(spec, ItemStack(Material.DIRT), emptySet()))
        assertTrue(runtime.matchesItem(spec, ItemStack(Material.STONE), setOf("material", "nonsense")))
        assertFalse(runtime.matchesItem(spec, null, emptySet()))
    }

    @Test
    fun `the projects are read from the root, metadata excluded`() {
        val runtime = runtime()
        assertEquals(listOf("quests"), runtime.projectIds())
        assertEquals(listOf("daily.yaml"), runtime.listFiles("quests", "quest_groups"))
        assertEquals("schema: quest_group/v1\n", runtime.readFile("quests", "quest_groups/daily.yaml"))
        assertNull(runtime.readFile("quests", ".artisan/project-id"))
        assertNull(runtime.readFile("nope", "manifest.yaml"))
    }

    @Test
    fun `a reload picks up a lang file written since the start`() {
        val runtime = runtime()
        File(projects, "quests/lang/fr.yaml").writeText("reward:\n  done: \"Chapeau {quest}\"\n")
        runtime.runChain(player, listOf(mapOf("send_message" to "{i18n:reward.done}")), mapOf("quest" to "x"), "quests")
        runtime.reloadProjects()
        runtime.runChain(player, listOf(mapOf("send_message" to "{i18n:reward.done}")), mapOf("quest" to "x"), "quests")
        assertEquals(listOf("Bravo x", "Chapeau x"), messages())
    }

    private fun PlayerMock.nextMessage(): String? = nextComponentMessage()?.let { PlainTextComponentSerializer.plainText().serialize(it) }
}
