package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderCondition implements Condition {

    /**
     * How long a resolved placeholder is reused for the same player. A menu render tests
     * the same condition once per row, within a few milliseconds, and third-party
     * expansions may query a database on each resolution; a second of staleness on a
     * condition is invisible to the player.
     */
    static final long CACHE_TTL_MILLIS = 1000;
    private static final int CACHE_SWEEP_SIZE = 4096;

    private static final Map<CacheKey, Resolved> CACHE = new ConcurrentHashMap<>();

    private final String placeholder;
    private final String action;
    private final String value;

    public PlaceholderCondition(Map<String, Object> params) {
        this.placeholder = (String) params.get("placeholder");
        this.action = ((String) params.get("action")).toUpperCase();
        this.value = String.valueOf(params.get("value"));
    }

    @Override
    public boolean test(PlayerProfile profile) {
        return test(profile, Map.of());
    }

    @Override
    public boolean test(PlayerProfile profile, Map<String, String> contextPlaceholders) {
        Player player = Bukkit.getPlayer(profile.getId());
        if (player == null) return false;

        String resolvedPlaceholder = applyContext(placeholder, contextPlaceholders);
        String resolvedValue = applyContext(value, contextPlaceholders);

        String resolved = resolve(player, resolvedPlaceholder);
        resolvedValue = resolve(player, resolvedValue);

        return switch (action) {
            case "EQUALS_STRING" -> resolved.equals(resolvedValue);
            case "EQUALS_IGNORECASE_STRING" -> resolved.equalsIgnoreCase(resolvedValue);
            case "CONTAINS_STRING" -> resolved.contains(resolvedValue);
            case "DIFFERENT_STRING" -> !resolved.equals(resolvedValue);
            default -> compareNumeric(resolved, resolvedValue);
        };
    }

    private static String resolve(Player player, String text) {
        if (text.indexOf('%') < 0) {
            // Nothing to resolve: a literal value never reaches PlaceholderAPI.
            return text;
        }
        long now = System.currentTimeMillis();
        CacheKey key = new CacheKey(player.getUniqueId(), text);
        Resolved cached = CACHE.get(key);
        if (cached != null && now - cached.at() < CACHE_TTL_MILLIS) {
            return cached.text();
        }
        String resolved = PlaceholderAPI.setPlaceholders(player, text);
        if (CACHE.size() >= CACHE_SWEEP_SIZE) {
            CACHE.entrySet().removeIf(entry -> now - entry.getValue().at() >= CACHE_TTL_MILLIS);
        }
        CACHE.put(key, new Resolved(resolved, now));
        return resolved;
    }

    /** Drops every cached resolution, for a player leaving or a test. */
    public static void invalidate(UUID playerId) {
        CACHE.keySet().removeIf(key -> key.playerId().equals(playerId));
    }

    private String applyContext(String text, Map<String, String> context) {
        String result = text;
        for (Map.Entry<String, String> entry : context.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    private boolean compareNumeric(String resolved, String resolvedValue) {
        try {
            double a = Double.parseDouble(resolved);
            double b = Double.parseDouble(resolvedValue);
            return switch (action) {
                case "EQUAL_TO" -> a == b;
                case "SUPERIOR" -> a > b;
                case "SUPERIOR_OR_EQUAL" -> a >= b;
                case "LOWER" -> a < b;
                case "LOWER_OR_EQUAL" -> a <= b;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private record CacheKey(UUID playerId, String text) {}

    private record Resolved(String text, long at) {}
}
