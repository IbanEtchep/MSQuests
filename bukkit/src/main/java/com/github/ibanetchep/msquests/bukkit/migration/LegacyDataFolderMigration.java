package com.github.ibanetchep.msquests.bukkit.migration;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Carries the data folder over from the plugin's former name. Bukkit derives the data folder
 * from the descriptor {@code name}, so renaming MSQuests to ArtisanQuests would otherwise start
 * the plugin on an empty {@code plugins/ArtisanQuests/} and ignore the existing configs, quests,
 * lang files and H2 database.
 *
 * <p>The legacy folder is copied, not moved: rolling back to the old jar keeps working. The copy
 * goes through a staging folder renamed into place at the end, so a crash mid-copy never leaves
 * a half-filled target that the next start would take for a finished migration.
 */
public final class LegacyDataFolderMigration {

    public static final String LEGACY_NAME = "MSQuests";

    public enum Outcome { COPIED, NO_LEGACY_FOLDER, ALREADY_MIGRATED }

    private LegacyDataFolderMigration() {
    }

    /**
     * Copies {@code legacy} into {@code target} when {@code legacy} exists and {@code target} is
     * missing or empty. Never overwrites: a non-empty target means the migration already ran or
     * the admin set the new folder up by hand.
     */
    public static Outcome migrate(Path legacy, Path target, Logger logger) throws IOException {
        if (!Files.isDirectory(legacy)) {
            return Outcome.NO_LEGACY_FOLDER;
        }
        if (Files.isDirectory(target) && !isEmpty(target)) {
            return Outcome.ALREADY_MIGRATED;
        }

        Path staging = target.resolveSibling(target.getFileName() + ".migrating");
        deleteTree(staging);
        copyTree(legacy, staging);
        Files.deleteIfExists(target);
        Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
        logger.info("Copied the data folder of the former plugin name: " + legacy + " -> " + target
                + ". The old folder is kept for rollback and can be deleted once everything works.");
        return Outcome.COPIED;
    }

    private static boolean isEmpty(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void copyTree(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()),
                        StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
