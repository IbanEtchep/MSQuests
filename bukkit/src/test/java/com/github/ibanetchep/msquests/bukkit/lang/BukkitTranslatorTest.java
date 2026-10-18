package com.github.ibanetchep.msquests.bukkit.lang;

import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import net.artisanmc.modules.api.translation.MessageBundle;
import net.artisanmc.modules.api.translation.MessageBundleDeclaration;
import net.artisanmc.modules.api.translation.TranslationsApi;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
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

        MessageBundleDeclaration declaration = translations.declarations.get(0);
        assertEquals(BukkitTranslator.NAMESPACE, declaration.getNamespace());
        assertEquals("fr_FR", declaration.getDefaultLang());
        Map<String, Map<String, String>> tables = declaration.getSource().load();
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

    /** Artisan's translations reduced to what the translator uses: one table per language, no fallback. */
    private static final class RecordingTranslations implements TranslationsApi {
        final List<MessageBundleDeclaration> declarations = new ArrayList<>();
        final Map<String, MessageBundle> bundles = new LinkedHashMap<>();

        @Override
        public String getDefaultLang() {
            return "en";
        }

        @Override
        public String langOf(CommandSender sender) {
            return "en";
        }

        @Override
        public MessageBundle register(MessageBundleDeclaration declaration) {
            if (bundles.containsKey(declaration.getNamespace())) throw new IllegalStateException("registered twice");
            declarations.add(declaration);
            Map<String, Map<String, String>> tables;
            try {
                tables = declaration.getSource().load();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            MessageBundle bundle = new MessageBundle() {
                @Override public String getNamespace() { return declaration.getNamespace(); }
                @Override public Set<String> getLanguages() { return tables.keySet(); }
                @Override public String getDefaultLang() { return declaration.getDefaultLang(); }
                @Override public String langOf(CommandSender viewer) { return getDefaultLang(); }
                @Override public @Nullable String find(String lang, String key) {
                    Map<String, String> table = tables.get(lang);
                    return table == null ? null : table.get(key);
                }
                @Override public String raw(String lang, String key) {
                    String text = find(lang, key);
                    return text == null ? key : text;
                }
                @Override public String format(String template, Map<String, ?> placeholders) { return template; }
                @Override public Component get(String lang, String key, Map<String, ?> placeholders) {
                    return Component.text(raw(lang, key));
                }
                @Override public void reload() {}
            };
            bundles.put(declaration.getNamespace(), bundle);
            return bundle;
        }

        @Override
        public void unregister(String namespace) {
            bundles.remove(namespace);
        }

        @Override
        public @Nullable MessageBundle bundle(String namespace) {
            return bundles.get(namespace);
        }

        @Override
        public Set<String> namespaces() {
            return bundles.keySet();
        }
    }
}
