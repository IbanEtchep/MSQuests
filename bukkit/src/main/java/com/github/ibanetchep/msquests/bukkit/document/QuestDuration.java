package com.github.ibanetchep.msquests.bukkit.document;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Quest durations: seconds as a number, or {@code 24h}, {@code 1h30m}, {@code 2d}, {@code 90s}. */
final class QuestDuration {

    private static final Pattern PART = Pattern.compile("(\\d+)([dhms])");

    private QuestDuration() {
    }

    static @Nullable Long parse(@Nullable Object value, String path) {
        if (value == null) return null;
        if (value instanceof Number n) {
            if (n.longValue() < 0) throw new DocumentException(path, "a duration cannot be negative");
            return n.longValue();
        }
        String text = value.toString().trim().toLowerCase(Locale.ROOT).replace(" ", "");
        if (text.matches("\\d+")) return Long.parseLong(text);
        Matcher m = PART.matcher(text);
        long seconds = 0;
        int end = 0;
        while (m.find()) {
            if (m.start() != end) break;
            long amount = Long.parseLong(m.group(1));
            seconds += switch (m.group(2)) {
                case "d" -> amount * 86_400;
                case "h" -> amount * 3_600;
                case "m" -> amount * 60;
                default -> amount;
            };
            end = m.end();
        }
        if (end == 0 || end != text.length()) {
            throw new DocumentException(path, "expected a duration such as 24h, 1h30m or 90s, got '" + value + "'");
        }
        return seconds;
    }

    /** Shortest exact form: 86400 → 24h, 5400 → 90m, 45 → 45s. */
    static String format(long seconds) {
        if (seconds > 0 && seconds % 3_600 == 0) return seconds / 3_600 + "h";
        if (seconds > 0 && seconds % 60 == 0) return seconds / 60 + "m";
        return seconds + "s";
    }
}
