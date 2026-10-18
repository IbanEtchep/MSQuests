package com.github.ibanetchep.msquests.suite

import net.artisanmc.plugin.database.ModuleDatabase
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.plugin.PluginMock
import java.io.File
import java.nio.file.Files
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SuiteDatabaseTest {
    private lateinit var plugin: PluginMock

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin("MSQuests")
    }

    @AfterEach
    fun tearDown() = MockBukkit.unmock()

    @Test
    fun `without Artisan's environment the plugin's own H2 section is written and the pool opens under its folder`() {
        val db = SuiteDatabase.open(plugin)
        try {
            assertEquals("config.yml", db.source)
            assertEquals("h2", db.type)
            assertEquals("h2", plugin.config.getString("database.type"))
            db.dataSource().connection.use { assertTrue(it.isValid(1)) }
            assertTrue(File(plugin.dataFolder, "database/suite.mv.db").isFile)
        } finally {
            db.close()
        }
    }

    @Test
    fun `the H2 file of the plugin's previous pool is moved into database once`() {
        val dataFolder = Files.createTempDirectory("msquests_").toFile()
        val legacy = File(dataFolder, "database.mv.db").apply { writeText("old") }
        val db = ModuleDatabase.resolve("MSQuests", dataFolder, YamlConfiguration(), {}, File(dataFolder, "none.yml"), Logger.getLogger("test"))

        SuiteDatabase.moveLegacyH2(dataFolder, db, Logger.getLogger("test"))

        assertFalse(legacy.exists())
        assertEquals("old", File(dataFolder, "database/suite.mv.db").readText())

        File(dataFolder, "database.mv.db").writeText("newer")
        SuiteDatabase.moveLegacyH2(dataFolder, db, Logger.getLogger("test"))
        assertEquals("old", File(dataFolder, "database/suite.mv.db").readText(), "an existing file is never replaced")
        dataFolder.deleteRecursively()
    }
}
