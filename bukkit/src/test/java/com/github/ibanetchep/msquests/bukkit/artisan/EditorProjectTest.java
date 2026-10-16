package com.github.ibanetchep.msquests.bukkit.artisan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EditorProjectTest {

    @TempDir
    Path dataFolder;

    @Test
    void writesTheManifestOnFirstBoot() throws Exception {
        File dir = EditorProject.directory(dataFolder.toFile());

        assertTrue(EditorProject.writeIfAbsent(dir, "fr_FR"));

        assertEquals("schema: manifest/v1\ndefault_lang: fr\nlanguages: [fr]\n",
                Files.readString(dir.toPath().resolve("manifest.yaml")));
        assertTrue(Files.isDirectory(dir.toPath().resolve("quest_groups")));
        assertFalse(Files.exists(dataFolder.resolve("editor.tmp")));
    }

    @Test
    void neverTouchesAnExistingProject() throws Exception {
        Path dir = dataFolder.resolve("editor");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("manifest.yaml"), "schema: manifest/v1\ndefault_lang: en\n");

        assertFalse(EditorProject.writeIfAbsent(dir.toFile(), "fr_FR"));

        assertEquals("schema: manifest/v1\ndefault_lang: en\n", Files.readString(dir.resolve("manifest.yaml")));
        assertFalse(Files.exists(dir.resolve("quest_groups")));
    }

    @Test
    void fillsAnEmptyDirectory() throws Exception {
        Path dir = dataFolder.resolve("editor");
        Files.createDirectories(dir);

        assertTrue(EditorProject.writeIfAbsent(dir.toFile(), "en_EN"));
        assertTrue(Files.exists(dir.resolve("manifest.yaml")));
    }

    @Test
    void defaultLangIsTheLanguagePartOfTheSetting() {
        assertEquals("fr", EditorProject.defaultLang("fr_FR"));
        assertEquals("en", EditorProject.defaultLang("en-US"));
        assertEquals("en", EditorProject.defaultLang("???"));
        assertEquals("en", EditorProject.defaultLang(null));
    }
}
