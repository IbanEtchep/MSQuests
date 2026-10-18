package com.github.ibanetchep.msquests.bukkit.lang;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Artisan's translations ({@code TranslationsApi}) as the translator uses them: one bundle,
 * replaced on reload, read in the plugin's language.
 */
public interface TranslationsPort {

    /** Registers the bundle and returns its lookup in {@code language}: the text, or null when the key is unknown. */
    Lookup register(Plugin owner, String namespace, String language, Supplier<Map<String, Map<String, String>>> tables);

    void unregister(String namespace);

    @FunctionalInterface
    interface Lookup {
        @Nullable String find(String key);
    }
}
