package com.github.ibanetchep.msquests.bukkit.artisan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The Artisan project MSQuests owns: {@code plugins/MSQuests/editor/}, registered as a project
 * root so the editor lists it under the module. It holds the {@code quest_group} Documents
 * ({@code quest_groups/}) once a server converts its {@code quests/*.yml}; until then it only
 * carries its manifest, and the quests keep loading from {@code quests/*.yml}.
 */
public final class EditorProject {

    public static final String PROJECT_ID = "quests";
    public static final String DIR = "editor";

    private EditorProject() {
    }

    public static File directory(File dataFolder) {
        return new File(dataFolder, DIR);
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
