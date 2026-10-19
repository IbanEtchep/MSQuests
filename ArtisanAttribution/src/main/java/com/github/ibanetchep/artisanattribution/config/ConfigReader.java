package com.github.ibanetchep.artisanattribution.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Reads config.yml into an {@link AttributionConfig}. What makes the plugin unusable (no source,
 * a source without a name, a malformed id or menu) throws {@link ConfigException}; what can be
 * repaired (an unknown material, a negative delay) is repaired and reported in {@code warnings}.
 */
public final class ConfigReader {

    public static final String DEFAULT_MATERIAL = "PAPER";

    private static final Pattern SOURCE_ID = Pattern.compile("^[a-z0-9_]{1,64}$");
    private static final Pattern MENU_REF = Pattern.compile("^([a-z0-9][a-z0-9_-]*:)?[A-Za-z0-9_][A-Za-z0-9_.-]*$");

    private ConfigReader() {
    }

    /**
     * @param materialExists whether a material name is known to the server, so a typo shows
     *     a paper button and a warning rather than an empty slot
     */
    public static AttributionConfig read(ConfigurationSection root, Predicate<String> materialExists,
                                         List<String> warnings) throws ConfigException {
        String menu = root.getString("menu", "attribution:attribution").trim();
        if (!MENU_REF.matcher(menu).matches()) {
            throw new ConfigException("menu: '" + menu + "' is not a menu reference (project:menu)");
        }

        ConfigurationSection sourcesSection = root.getConfigurationSection("sources");
        if (sourcesSection == null || sourcesSection.getKeys(false).isEmpty()) {
            throw new ConfigException("sources: at least one source is needed");
        }
        Map<String, AttributionSource> sources = new LinkedHashMap<>();
        for (String id : sourcesSection.getKeys(false)) {
            sources.put(id, readSource(id, sourcesSection, materialExists, warnings));
        }

        return new AttributionConfig(
                menu,
                readPrompt(root.getConfigurationSection("prompt"), warnings),
                actions(root.get("on_answer")),
                Collections.unmodifiableMap(sources));
    }

    private static AttributionSource readSource(String id, ConfigurationSection sources,
                                                Predicate<String> materialExists,
                                                List<String> warnings) throws ConfigException {
        String path = "sources." + id;
        if (!SOURCE_ID.matcher(id).matches()) {
            throw new ConfigException(path + ": the id must be a lowercase word (a-z, 0-9, _), 64 characters at most");
        }
        ConfigurationSection section = sources.getConfigurationSection(id);
        if (section == null) {
            throw new ConfigException(path + ": expected name, material… under the id");
        }

        LocalizedText name = LocalizedText.parse(plain(section.get("name")));
        if (name == null || name.isEmpty()) {
            throw new ConfigException(path + ".name: a text, or { en: \"…\", fr: \"…\" }, is required");
        }

        LocalizedText description = LocalizedText.EMPTY;
        if (section.contains("description")) {
            description = LocalizedText.parse(plain(section.get("description")));
            if (description == null) {
                throw new ConfigException(path + ".description: expected a text, or { en: \"…\", fr: \"…\" }");
            }
        }

        String material = section.getString("material", DEFAULT_MATERIAL).trim().toUpperCase(Locale.ROOT);
        if (!materialExists.test(material)) {
            warnings.add(path + ".material: unknown material '" + material + "', " + DEFAULT_MATERIAL + " is shown instead");
            material = DEFAULT_MATERIAL;
        }

        return new AttributionSource(id, name, description, material, actions(section.get("actions")));
    }

    private static PromptSettings readPrompt(@Nullable ConfigurationSection section, List<String> warnings) {
        PromptSettings d = PromptSettings.DEFAULTS;
        if (section == null) return d;
        long delay = section.getLong("delay_ticks", d.delayTicks());
        if (delay < 0) {
            warnings.add("prompt.delay_ticks: " + delay + " is negative, 0 is used");
            delay = 0;
        }
        return new PromptSettings(
                section.getBoolean("on_join", d.onJoin()),
                section.getBoolean("repeat_until_answered", d.repeatUntilAnswered()),
                section.getBoolean("new_players_only", d.newPlayersOnly()),
                delay,
                section.getBoolean("close_on_answer", d.closeOnAnswer()));
    }

    /** An action chain as Artisan reads it: null when absent or empty. */
    private static @Nullable Object actions(@Nullable Object value) {
        Object chain = plain(value);
        if (chain == null) return null;
        if (chain instanceof List<?> list && list.isEmpty()) return null;
        if (chain instanceof String text && text.isBlank()) return null;
        return chain;
    }

    /** Bukkit's sections turned into plain maps, recursively, the shape Artisan's YAML readers give. */
    static @Nullable Object plain(@Nullable Object value) {
        if (value instanceof ConfigurationSection section) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : section.getKeys(false)) {
                map.put(key, plain(section.get(key)));
            }
            return map;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((k, v) -> copy.put(String.valueOf(k), plain(v)));
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) copy.add(plain(item));
            return copy;
        }
        return value;
    }
}
