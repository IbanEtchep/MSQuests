package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BukkitTranslatorTest {

    @TempDir
    Path langFolder;

    private final RecordingTranslations translations = new RecordingTranslations();

    private BukkitTranslator translator() {
        return new BukkitTranslator(langFolder.toFile(), "fr_FR",
                locale -> getClass().getResourceAsStream("/lang/" + locale + ".yml"),
                Logger.getAnonymousLogger(), mock(Plugin.class), translations);
    }

    @Test
    void theFileIsServedByArtisanInTheConfiguredLanguage() throws Exception {
        translator().load();

        Registered declaration = translations.declarations.get(0);
        assertEquals(BukkitTranslator.NAMESPACE, declaration.namespace());
        assertEquals("fr_FR", declaration.language());
        Map<String, Map<String, String>> tables = declaration.tables().get();
        assertEquals(Set.of("fr_FR"), tables.keySet());
        assertTrue(tables.get("fr_FR").get("quest_admin.reload").contains("rechargées"));
    }

    @Test
    void getRawReadsTheBundleAndKeepsItsMissingText() {
        BukkitTranslator translator = translator();
        translator.load();

        assertTrue(translator.getRaw(TranslationKey.QUEST_ADMIN_RELOAD).contains("rechargées"));
        assertEquals("Missing translation: nope.nothing", translator.getRaw("nope.nothing"));
    }

    @Test
    void aReloadReplacesTheBundle() {
        translator().load();
        translator().load();

        assertEquals(2, translations.declarations.size());
        assertEquals(Set.of(BukkitTranslator.NAMESPACE), translations.bundles.keySet());
    }

    private record Registered(String namespace, String language, Supplier<Map<String, Map<String, String>>> tables) {
    }

    /** Artisan's translations reduced to what the translator uses: one table per language, no fallback. */
    private static final class RecordingTranslations implements TranslationsPort {
        final List<Registered> declarations = new ArrayList<>();
        final Map<String, Lookup> bundles = new LinkedHashMap<>();

        @Override
        public Lookup register(Plugin owner, String namespace, String language, Supplier<Map<String, Map<String, String>>> tables) {
            if (bundles.containsKey(namespace)) throw new IllegalStateException("registered twice");
            declarations.add(new Registered(namespace, language, tables));
            Map<String, String> table = tables.get().getOrDefault(language, Map.of());
            Lookup lookup = table::get;
            bundles.put(namespace, lookup);
            return lookup;
        }

        @Override
        public void unregister(String namespace) {
            bundles.remove(namespace);
        }
    }
}
