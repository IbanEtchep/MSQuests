package com.github.ibanetchep.artisanattribution.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigReaderTest {

    private static final Set<String> MATERIALS = Set.of("PAPER", "COMPASS", "PLAYER_HEAD", "LIGHT_BLUE_DYE",
            "MUSIC_DISC_CAT", "RED_DYE", "BOOK");

    private static YamlConfiguration yaml(String text) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(text);
        return yaml;
    }

    private static AttributionConfig read(String text, List<String> warnings) throws Exception {
        return ConfigReader.read(yaml(text), MATERIALS::contains, warnings);
    }

    @Test
    void readsTheShippedConfig() throws Exception {
        String shipped;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (in == null) throw new IOException("config.yml not on the test classpath");
            shipped = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<String> warnings = new ArrayList<>();
        AttributionConfig config = read(shipped, warnings);

        assertEquals(List.of(), warnings);
        assertEquals("attribution:attribution", config.menu());
        assertEquals(PromptSettings.DEFAULTS, config.prompt());
        assertNull(config.onAnswer());
        assertEquals(List.of("web_search", "friend", "discord", "tiktok", "youtube", "server_list", "other"),
                config.orderedSources().stream().map(AttributionSource::id).toList());
        AttributionSource friend = config.source("friend");
        assertEquals("Recommandation d'un ami", friend.name().resolve("fr", "en"));
        assertEquals("A friend told me", friend.name().resolve("de", "en"));
        assertEquals("Discord", config.source("discord").name().resolve("fr", "en"));
        assertEquals("", config.source("discord").description().resolve("fr", "en"));
    }

    @Test
    void keepsTheActionChainsAsPlainData() throws Exception {
        AttributionConfig config = read("""
                on_answer:
                  - send_message: "Thanks {player}"
                sources:
                  friend:
                    name: Friend
                    actions:
                      - run_command:
                          command: "eco give {player} 100"
                          as: console
                """, new ArrayList<>());

        assertEquals(List.of(Map.of("send_message", "Thanks {player}")), config.onAnswer());
        assertEquals(List.of(Map.of("run_command", Map.of("command", "eco give {player} 100", "as", "console"))),
                config.source("friend").actions());
    }

    @Test
    void readsThePromptSettings() throws Exception {
        AttributionConfig config = read("""
                prompt:
                  on_join: true
                  repeat_until_answered: false
                  new_players_only: true
                  delay_ticks: 0
                  close_on_answer: false
                sources:
                  a: { name: A }
                """, new ArrayList<>());
        assertEquals(new PromptSettings(true, false, true, 0, false), config.prompt());
    }

    @Test
    void repairsWhatItCanAndSaysSo() throws Exception {
        List<String> warnings = new ArrayList<>();
        AttributionConfig config = read("""
                prompt: { delay_ticks: -5 }
                sources:
                  a: { name: A, material: NOT_A_BLOCK }
                """, warnings);
        assertEquals("PAPER", config.source("a").material());
        assertEquals(0, config.prompt().delayTicks());
        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("sources.a.material"));
    }

    @Test
    void refusesAConfigItCannotUse() {
        assertThrows(ConfigException.class, () -> read("sources: {}", new ArrayList<>()));
        assertThrows(ConfigException.class, () -> read("sources:\n  Bad-Id: { name: X }", new ArrayList<>()));
        assertThrows(ConfigException.class, () -> read("sources:\n  a: { material: PAPER }", new ArrayList<>()));
        assertThrows(ConfigException.class, () -> read("sources:\n  a: { name: [x] }", new ArrayList<>()));
        assertThrows(ConfigException.class, () -> read("menu: 'a b'\nsources:\n  a: { name: A }", new ArrayList<>()));
    }
}
