package com.github.ibanetchep.msquests.bukkit.text.placeholder;

import com.github.ibanetchep.msquests.bukkit.lang.BukkitTranslator;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;

import java.util.Map;

public class PlaceholderEngine {

    private static final PlaceholderEngine INSTANCE = new PlaceholderEngine();

    public static PlaceholderEngine getInstance() {
        return INSTANCE;
    }

    public <T extends PlaceholderProvider> String apply(String template, T placeholderProvider) {
        if (template == null || placeholderProvider == null) return template;
        String result = template;

        Map<String, String> placeholders = placeholderProvider.getPlaceholders(BukkitTranslator.getInstance());

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return result;
    }
}
