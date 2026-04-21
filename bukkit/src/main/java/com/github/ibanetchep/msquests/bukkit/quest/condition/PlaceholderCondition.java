package com.github.ibanetchep.msquests.bukkit.quest.condition;

import com.github.ibanetchep.msquests.core.quest.condition.Condition;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlaceholderCondition implements Condition {

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

        String resolved = PlaceholderAPI.setPlaceholders(player, resolvedPlaceholder);
        resolvedValue = PlaceholderAPI.setPlaceholders(player, resolvedValue);

        return switch (action) {
            case "EQUALS_STRING" -> resolved.equals(resolvedValue);
            case "EQUALS_IGNORECASE_STRING" -> resolved.equalsIgnoreCase(resolvedValue);
            case "CONTAINS_STRING" -> resolved.contains(resolvedValue);
            case "DIFFERENT_STRING" -> !resolved.equals(resolvedValue);
            default -> compareNumeric(resolved, resolvedValue);
        };
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
}
