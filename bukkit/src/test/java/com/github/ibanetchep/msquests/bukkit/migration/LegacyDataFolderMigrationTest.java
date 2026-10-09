package com.github.ibanetchep.msquests.bukkit.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDataFolderMigrationTest {

    private static final Logger LOGGER = Logger.getLogger("test");

    @TempDir
    Path plugins;

    @Test
    void copiesNestedLegacyFolderAndKeepsTheOriginal() throws IOException {
        Path legacy = plugins.resolve("MSQuests");
        Files.createDirectories(legacy.resolve("quests"));
        Files.writeString(legacy.resolve("config.yml"), "language: fr");
        Files.writeString(legacy.resolve("quests/daily.yml"), "daily: {}");
        Path target = plugins.resolve("ArtisanQuests");

        var outcome = LegacyDataFolderMigration.migrate(legacy, target, LOGGER);

        assertEquals(LegacyDataFolderMigration.Outcome.COPIED, outcome);
        assertEquals("language: fr", Files.readString(target.resolve("config.yml")));
        assertEquals("daily: {}", Files.readString(target.resolve("quests/daily.yml")));
        assertTrue(Files.exists(legacy.resolve("config.yml")));
    }

    @Test
    void copiesIntoAnEmptyTargetFolder() throws IOException {
        Path legacy = plugins.resolve("MSQuests");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "language: fr");
        Path target = Files.createDirectories(plugins.resolve("ArtisanQuests"));

        assertEquals(LegacyDataFolderMigration.Outcome.COPIED,
                LegacyDataFolderMigration.migrate(legacy, target, LOGGER));
        assertTrue(Files.exists(target.resolve("config.yml")));
    }

    @Test
    void discardsALeftoverStagingFolderFromAnInterruptedRun() throws IOException {
        Path legacy = plugins.resolve("MSQuests");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "language: fr");
        Path staging = Files.createDirectories(plugins.resolve("ArtisanQuests.migrating"));
        Files.writeString(staging.resolve("partial.yml"), "half");
        Path target = plugins.resolve("ArtisanQuests");

        LegacyDataFolderMigration.migrate(legacy, target, LOGGER);

        assertTrue(Files.exists(target.resolve("config.yml")));
        assertFalse(Files.exists(target.resolve("partial.yml")));
        assertFalse(Files.exists(staging));
    }

    @Test
    void neverOverwritesAnExistingTarget() throws IOException {
        Path legacy = plugins.resolve("MSQuests");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "old");
        Path target = Files.createDirectories(plugins.resolve("ArtisanQuests"));
        Files.writeString(target.resolve("config.yml"), "new");

        assertEquals(LegacyDataFolderMigration.Outcome.ALREADY_MIGRATED,
                LegacyDataFolderMigration.migrate(legacy, target, LOGGER));
        assertEquals("new", Files.readString(target.resolve("config.yml")));
    }

    @Test
    void doesNothingOnAFreshInstall() throws IOException {
        Path target = plugins.resolve("ArtisanQuests");

        assertEquals(LegacyDataFolderMigration.Outcome.NO_LEGACY_FOLDER,
                LegacyDataFolderMigration.migrate(plugins.resolve("MSQuests"), target, LOGGER));
        assertFalse(Files.exists(target));
    }
}
