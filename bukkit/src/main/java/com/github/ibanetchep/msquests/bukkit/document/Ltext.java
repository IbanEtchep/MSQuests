package com.github.ibanetchep.msquests.bukkit.document;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Artisan {@code ltext} values: a plain string, or one string per language
 * ({@code {fr: "…", en: "…"}}). Until texts are resolved per player (a later lot), a group is
 * loaded in the project's default language.
 */
final class Ltext {

    private Ltext() {
    }

    /** The text in {@code lang}, else in the first language given; null when absent. */
    static @Nullable String resolve(@Nullable Object value, String lang, String path) {
        if (value == null) return null;
        if (value instanceof String text) return text;
        if (value instanceof Map<?, ?> byLang) {
            Object inLang = byLang.get(lang);
            if (inLang == null && !byLang.isEmpty()) inLang = byLang.values().iterator().next();
            if (inLang == null) return null;
            if (inLang instanceof String text) return text;
            throw new DocumentException(path, "a translation must be a string, got " + inLang);
        }
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        throw new DocumentException(path, "expected a text or {lang: text}, got " + value);
    }

    /** Raw MiniMessage becomes {@code {lang: text}}, unchanged: translated later in the editor. */
    static @Nullable Map<String, Object> of(@Nullable String text, String lang) {
        if (text == null) return null;
        Map<String, Object> ltext = new LinkedHashMap<>();
        ltext.put(lang, text);
        return ltext;
    }
}
