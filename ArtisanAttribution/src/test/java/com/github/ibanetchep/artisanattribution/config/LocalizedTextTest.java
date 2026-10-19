package com.github.ibanetchep.artisanattribution.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocalizedTextTest {

    private static LocalizedText text(Object value) {
        return LocalizedText.parse(value);
    }

    @Test
    void plainStringIsTheSameInEveryLanguage() {
        assertEquals("Discord", text("Discord").resolve("fr", "en"));
    }

    @Test
    void picksTheAskedLanguageThenTheDefaultThenTheFirst() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("de", "Freund");
        map.put("en", "Friend");
        map.put("fr", "Ami");
        LocalizedText friend = text(map);
        assertEquals("Ami", friend.resolve("fr", "en"));
        assertEquals("Friend", friend.resolve("es", "en"));
        assertEquals("Freund", friend.resolve("es", "it"));
    }

    @Test
    void aLanguageMatchesItsRegionVariants() {
        assertEquals("Ami", text(Map.of("fr", "Ami", "en", "Friend")).resolve("fr_FR", "en"));
        assertEquals("Ami", text(Map.of("fr_FR", "Ami", "en", "Friend")).resolve("fr", "en"));
        assertEquals("Ami", text(Map.of("fr-FR", "Ami", "en", "Friend")).resolve("FR_fr", "en"));
    }

    @Test
    void refusesWhatIsNotAText() {
        assertNull(text(12));
        assertNull(text(List.of("a")));
        assertNull(text(Map.of("en", List.of("a"))));
        assertNull(text(Map.of()));
    }
}
