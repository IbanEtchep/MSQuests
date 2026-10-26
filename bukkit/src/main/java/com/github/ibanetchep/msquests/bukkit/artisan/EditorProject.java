package com.github.ibanetchep.msquests.bukkit.artisan;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The folder {@code quests}, the module's own ({@code plugins/Artisan/modules/quests/}, registered
 * with {@code ProjectRootRegistry.moduleFolder}): the editor lists it under the installed module
 * Quests, the example template seeds it once ({@link MsQuestsArtisanModule#TEMPLATE}) and
 * {@code /msquests migrate} writes the converted {@code quests/*.yml} there. Its name is the one
 * the ordinary project had before, so an existing folder becomes the module's as it is. Quest
 * groups still load from every folder.
 */
public final class EditorProject {

    public static final String PROJECT_ID = "quests";
    /** MSQuests' own project before it became an ordinary one, moved once ({@link #moveLegacy}). */
    public static final String LEGACY_DIR = "editor";

    private EditorProject() {
    }

    /**
     * {@code <Artisan's data folder>/modules/quests}, where {@code moduleFolder} puts it: the path
     * used until the module registered its folder, and to decide whether one already exists.
     */
    public static File directory(File artisanDataFolder) {
        return new File(new File(artisanDataFolder, "modules"), PROJECT_ID);
    }

    /**
     * {@code plugins/MSQuests/editor/}, the project MSQuests registered itself before, when it
     * holds quest groups and {@code target} does not exist: {@code moduleFolder} moves it there.
     * Null otherwise: an empty one carries nothing, and an existing target already holds the
     * quests (handing both to {@code moduleFolder} would keep the old one as the module's folder).
     */
    public static @Nullable File legacyToMove(File dataFolder, File target) {
        File legacy = new File(dataFolder, LEGACY_DIR);
        String[] groups = new File(legacy, "quest_groups").list();
        if (groups == null || groups.length == 0 || target.exists()) return null;
        return legacy;
    }

    /**
     * Writes the manifest of an absent or empty project directory and returns true; leaves an
     * existing project untouched (the admin's work, or the editor's) and returns false. The
     * directory is built aside and moved in, so a crash never leaves a half-written project.
     *
     * @param language the plugin's {@code language} setting ({@code fr_FR}), whose language part
     *                 becomes the project's default language
     */
    public static boolean writeIfAbsent(File projectDir, String language) throws IOException {
        String[] existing = projectDir.list();
        if (existing != null && existing.length > 0) {
            return false;
        }
        Path tmp = projectDir.toPath().resolveSibling(projectDir.getName() + ".tmp");
        deleteRecursively(tmp);
        Files.createDirectories(tmp.resolve("quest_groups"));
        Files.writeString(tmp.resolve("manifest.yaml"), manifest(language), StandardCharsets.UTF_8);
        Files.deleteIfExists(projectDir.toPath());
        Files.move(tmp, projectDir.toPath());
        return true;
    }

    static String manifest(String language) {
        String lang = defaultLang(language);
        return "schema: manifest/v1\n"
                + "default_lang: " + lang + "\n"
                + "languages: [" + lang + "]\n";
    }

    /** {@code fr_FR} → {@code fr}; anything unusable → {@code en}. */
    public static String defaultLang(String language) {
        if (language == null) return "en";
        String head = language.split("[_-]", 2)[0].toLowerCase(Locale.ROOT);
        return head.matches("[a-z]{2,3}") ? head : "en";
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }
}
