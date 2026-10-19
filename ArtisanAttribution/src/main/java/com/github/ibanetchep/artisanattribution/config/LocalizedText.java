package com.github.ibanetchep.artisanattribution.config;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A text of config.yml: a plain string, the same in every language, or {@code {en: "...", fr: "..."}}.
 * {@link #resolve} picks the asked language, then the default one, then the first given.
 */
public final class LocalizedText {

    public static final LocalizedText EMPTY = new LocalizedText(null, Map.of());

    private final @Nullable String plain;
    private final Map<String, String> byLang;

    private LocalizedText(@Nullable String plain, Map<String, String> byLang) {
        this.plain = plain;
        this.byLang = byLang;
    }

    public static LocalizedText of(String text) {
        return new LocalizedText(text, Map.of());
    }

    /**
     * Reads a config value: a string, or a map of language to string. Null for anything else
     * (a number, a list, a map holding something else than strings).
     */
    public static @Nullable LocalizedText parse(@Nullable Object value) {
        if (value instanceof String text) return of(text);
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) return null;
        Map<String, String> byLang = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof String text)) return null;
            byLang.put(normalize(String.valueOf(entry.getKey())), text);
        }
        return new LocalizedText(null, Map.copyOf(byLang));
    }

    /**
     * The text in {@code lang} ({@code fr} matches {@code fr_FR} and the reverse), else in
     * {@code defaultLang}, else the first language given.
     */
    public String resolve(String lang, String defaultLang) {
        if (plain != null) return plain;
        String found = find(lang);
        if (found == null) found = find(defaultLang);
        if (found == null) found = byLang.values().stream().findFirst().orElse("");
        return found;
    }

    private @Nullable String find(String lang) {
        String wanted = normalize(lang);
        String exact = byLang.get(wanted);
        if (exact != null) return exact;
        String base = base(wanted);
        for (Map.Entry<String, String> entry : byLang.entrySet()) {
            if (base(entry.getKey()).equals(base)) return entry.getValue();
        }
        return null;
    }

    private static String normalize(String lang) {
        return lang.trim().replace('-', '_').toLowerCase(Locale.ROOT);
    }

    private static String base(String lang) {
        int separator = lang.indexOf('_');
        return separator > 0 ? lang.substring(0, separator) : lang;
    }

    public boolean isEmpty() {
        return plain == null ? byLang.isEmpty() : plain.isEmpty();
    }
}
