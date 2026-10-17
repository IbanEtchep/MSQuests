package com.github.ibanetchep.msquests.bukkit.document;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns MSQuests actions and conditions (the {@code quests/*.yml} format: {@code type: command},
 * {@code type: permission}…) into Artisan's keyed wire (ADR {@code structured-action-wire}), which
 * is all the plugin runs since the house actions and conditions are gone. Three readers use it:
 * {@code quests/*.yml} at every load, the migration that writes the Documents, and the Document
 * reader for steps an earlier migration left in the MSQuests format.
 *
 * <table>
 *   <caption>Actions</caption>
 *   <tr><td>{@code command}</td><td>{@code run_command} as console</td></tr>
 *   <tr><td>{@code player_command}</td><td>{@code run_command} as console, {@code %player%} → {@code {player}}</td></tr>
 *   <tr><td>{@code give_item}</td><td>{@code give_item} ({@code amount} → {@code count})</td></tr>
 *   <tr><td>{@code message}</td><td>{@code send_message}; {@code objective_template} → the group's {@code objective_line}</td></tr>
 *   <tr><td>{@code title}</td><td>{@code show_title}</td></tr>
 *   <tr><td>{@code action_bar}</td><td>{@code quests:action_bar}</td></tr>
 *   <tr><td>{@code boss_bar}</td><td>{@code quests:boss_bar}</td></tr>
 * </table>
 *
 * <p>An action with {@code conditions} becomes {@code if: <conditions> do: [<action>]}. Conditions:
 * {@code permission} → {@code has_permission}; {@code placeholder} → {@code placeholder_equals},
 * {@code placeholder_match} or {@code expression} (the only one that reads the chain's bindings,
 * such as the quest's name).
 *
 * <p>Texts keep their MiniMessage; the MSQuests placeholders a chain now binds ({@code %quest_name%},
 * {@code %objective_progress%}…) become {@code {quest_name}}…, and {@code %msquests_…%} becomes
 * {@code %quests_…%}. What is converted with a change of behaviour is noted ({@link #notes()});
 * an unknown action type or a missing required field is refused, as it was before.
 */
public final class LegacyActions {

    /**
     * Bindings of a chain ({@code ChainContext#args}) that MSQuests texts wrote as {@code %name%}.
     * {@code player} is bound by Artisan.
     */
    static final Set<String> BINDINGS = Set.of(
            "player", "actor_id", "actor_name", "group_key", "group_name", "group_description",
            "quest_id", "quest_key", "quest_name", "quest_description", "quest_status", "quest_status_prefix",
            "quest_status_suffix", "quest_actor_name", "quest_actor_type", "quest_stage_index", "quest_stage_count",
            "quest_tier", "quest_tier_name", "stage_name", "stage_key", "objective_name", "objective_target",
            "objective_progress", "objective_progress_percent", "objective_status", "objective_status_prefix",
            "objective_status_suffix", "objectives");

    /** The legacy hooks of {@code quests/*.yml} ({@code actions.<hook>}). */
    static final List<String> LEGACY_HOOKS = List.of(
            "quest_start", "quest_complete", "objective_progress", "objective_complete",
            "quest_distribution", "actor_load", "all_period_quests_complete");

    private static final Pattern PERCENT_TOKEN = Pattern.compile("%([A-Za-z0-9_]+)%");
    private static final Pattern SINGLE_PERCENT_TOKEN = Pattern.compile("%[^%\\s]+%");

    /** Rewards: the chain and the lines menus show (the names the actions had). */
    public record Rewards(List<Object> steps, List<String> display) {
    }

    private final Function<String, String> messageKeys;
    private final Set<String> notes = new LinkedHashSet<>();
    private @Nullable String objectiveLine;

    /**
     * @param messageKeys text of a MSQuests lang key ({@code message_key}, {@code title_key}…), null
     *                    when unknown; the converted action carries the text itself
     */
    public LegacyActions(Function<String, String> messageKeys) {
        this.messageKeys = messageKeys;
    }

    /** What the conversion changed or could not keep, one line each, in order and without repeats. */
    public List<String> notes() {
        return List.copyOf(notes);
    }

    /** The first {@code objective_template} met, converted; null when no message had one. */
    public @Nullable String objectiveLine() {
        return objectiveLine;
    }

    /** True for a step in the MSQuests format: a keyed step never has a {@code type} key. */
    public static boolean isLegacy(@Nullable Object step) {
        return step instanceof Map<?, ?> map && map.containsKey("type");
    }

    // ----------------------------------------------------------------- groups

    /**
     * Rewrites the tree of a {@code quests/*.yml} group in place: hooks, rewards (a chain, their names
     * appended to the quest {@code description}), quest {@code conditions} (→ {@code requirements}), objective
     * {@code conditions} and the group's {@code objective_line}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> upgradeGroup(Map<String, Object> group) {
        String key = String.valueOf(group.get("key"));
        if (group.get("actions") instanceof Map<?, ?> hooks) {
            Map<String, Object> converted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> hook : hooks.entrySet()) {
                String path = key + ".actions." + hook.getKey();
                converted.put(String.valueOf(hook.getKey()), actions(list(hook.getValue(), path), path));
            }
            group.put("actions", converted);
        }

        if (group.get("quests") instanceof List<?> quests) {
            for (Object item : quests) {
                if (!(item instanceof Map<?, ?> raw)) continue;
                Map<String, Object> quest = (Map<String, Object>) raw;
                String path = key + "/" + quest.get("key");
                // A file already holding chains (saved after the conversion) is left as is.
                if (quest.get("rewards") instanceof List<?> legacy && legacy.stream().anyMatch(LegacyActions::isLegacy)) {
                    Rewards rewards = rewards(legacy, path + ".rewards");
                    quest.put("rewards", rewards.steps());
                    if (!rewards.display().isEmpty()) {
                        Object description = quest.get("description");
                        List<String> lines = new ArrayList<>();
                        if (description != null && !description.toString().isBlank()) lines.add(description.toString());
                        lines.addAll(rewards.display());
                        quest.put("description", String.join("\n", lines));
                    }
                }
                if (quest.containsKey("conditions")) {
                    Object conditions = quest.remove("conditions");
                    quest.put("requirements", conditions(list(conditions, path + ".conditions"), path + ".conditions"));
                }
                upgradeObjectives(quest, path);
            }
        }

        if (objectiveLine != null && !group.containsKey("objective_line")) {
            group.put("objective_line", objectiveLine);
        }
        return group;
    }

    @SuppressWarnings("unchecked")
    private void upgradeObjectives(Map<String, Object> quest, String path) {
        if (!(quest.get("stages") instanceof List<?> stages)) return;
        for (Object stage : stages) {
            if (!(stage instanceof Map<?, ?> stageMap) || !(stageMap.get("objectives") instanceof List<?> objectives)) continue;
            for (Object item : objectives) {
                if (!(item instanceof Map<?, ?> raw)) continue;
                Map<String, Object> objective = (Map<String, Object>) raw;
                if (objective.get("conditions") instanceof List<?> conditions) {
                    String objectivePath = path + "." + stageMap.get("key") + "." + objective.get("key") + ".conditions";
                    objective.put("conditions", conditions(conditions, objectivePath));
                }
            }
        }
    }

    // ---------------------------------------------------------------- actions

    /** A hook: each MSQuests action becomes one step; keyed steps pass through unchanged. */
    public List<Object> actions(List<?> legacy, String path) {
        List<Object> steps = new ArrayList<>();
        for (int i = 0; i < legacy.size(); i++) {
            Object item = legacy.get(i);
            steps.add(isLegacy(item) ? step(map(item), path + "[" + i + "]") : item);
        }
        return steps;
    }

    /** Rewards: the steps, and the {@code name} of each action as a display line. */
    public Rewards rewards(List<?> legacy, String path) {
        List<String> display = new ArrayList<>();
        for (Object item : legacy) {
            if (!isLegacy(item)) continue;
            Object name = map(item).get("name");
            if (name != null && !name.toString().isBlank()) {
                display.add(name.toString());
            } else {
                notes.add("Reward without a name at " + path + ": not listed in the quest description.");
            }
        }
        return new Rewards(actions(legacy, path), display);
    }

    private Object step(Map<String, Object> action, String path) {
        String type = string(action.get("type"));
        Object step = switch (type == null ? "" : type.toLowerCase(Locale.ROOT)) {
            case "command" -> runCommand(required(action, "command", path));
            case "player_command" -> runCommand(required(action, "command", path));
            case "give_item" -> giveItem(action, path);
            case "message" -> message(action, path);
            case "title" -> title(action, path);
            case "action_bar" -> Map.of("quests:action_bar", text(action, "message", "message_key", path));
            case "boss_bar" -> bossBar(action, path);
            default -> throw new IllegalArgumentException(path + ": unknown action type '" + type + "'");
        };

        if (action.get("conditions") instanceof List<?> conditions && !conditions.isEmpty()) {
            Map<String, Object> branch = new LinkedHashMap<>();
            branch.put("if", conditions(conditions, path + ".conditions"));
            branch.put("do", List.of(step));
            return branch;
        }
        return step;
    }

    private Object runCommand(String command) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("command", convertText(command));
        args.put("as", "console");
        return Map.of("run_command", args);
    }

    private Object giveItem(Map<String, Object> action, String path) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("material", required(action, "material", path).toUpperCase(Locale.ROOT));
        Object amount = action.get("amount");
        args.put("count", amount instanceof Number n ? n.intValue() : Integer.parseInt(required(action, "amount", path)));
        notes.add("give_item (" + path + "): items that do not fit in the inventory are no longer dropped at the"
                + " player's feet (Artisan's give_item keeps what fits).");
        return Map.of("give_item", args);
    }

    private Object message(Map<String, Object> action, String path) {
        String template = action.get("objective_template_key") != null
                ? keyText(string(action.get("objective_template_key")), path)
                : string(action.get("objective_template"));
        if (template != null) {
            String line = convertText(template);
            if (objectiveLine == null) {
                objectiveLine = line;
            } else if (!objectiveLine.equals(line)) {
                notes.add("objective_template (" + path + ") differs from an earlier one: the group keeps one"
                        + " objective_line, the first (" + objectiveLine + ").");
            }
        }
        return Map.of("send_message", text(action, "message", "message_key", path));
    }

    private Object title(Map<String, Object> action, String path) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("title", text(action, "title", "title_key", path));
        if (action.get("subtitle") != null || action.get("subtitle_key") != null) {
            args.put("subtitle", text(action, "subtitle", "subtitle_key", path));
        }
        return Map.of("show_title", args);
    }

    private Object bossBar(Map<String, Object> action, String path) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("text", text(action, "message", "message_key", path));
        putIfSet(args, "color", lower(action.get("color")));
        putIfSet(args, "style", lower(action.get("style")));
        putIfSet(args, "show_progress", action.get("show_progress"));
        putIfSet(args, "duration", action.get("duration"));
        return Map.of("quests:boss_bar", args);
    }

    // ------------------------------------------------------------- conditions

    /** Conditions: each MSQuests condition becomes one Artisan condition; keyed ones pass through. */
    public List<Object> conditions(List<?> legacy, String path) {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < legacy.size(); i++) {
            Object item = legacy.get(i);
            if (!isLegacy(item)) {
                out.add(item);
                continue;
            }
            Object condition = condition(map(item), path + "[" + i + "]");
            if (condition != null) out.add(condition);
        }
        return out;
    }

    private @Nullable Object condition(Map<String, Object> condition, String path) {
        String type = string(condition.get("type"));
        switch (type == null ? "" : type.toLowerCase(Locale.ROOT)) {
            case "permission":
                return Map.of("has_permission", required(condition, "permission", path));
            case "placeholder":
                return placeholder(condition, path);
            default:
                // MSQuests ignored a condition it did not know: it held.
                notes.add("Unknown condition type '" + type + "' at " + path + ": dropped, as MSQuests ignored it.");
                return null;
        }
    }

    private Object placeholder(Map<String, Object> condition, String path) {
        String placeholder = convertText(required(condition, "placeholder", path));
        String value = convertText(String.valueOf(condition.get("value")));
        String action = required(condition, "action", path).toUpperCase(Locale.ROOT);

        boolean literal = !value.contains("{") && !placeholder.contains("{")
                && SINGLE_PERCENT_TOKEN.matcher(placeholder).matches();
        switch (action) {
            case "EQUALS_STRING":
                if (literal) return Map.of("placeholder_equals", entries("placeholder", placeholder, "value", value));
                return Map.of("expression", operand(placeholder, path) + " == " + operand(value, path));
            case "DIFFERENT_STRING":
                if (literal) return Map.of("not", Map.of("placeholder_equals", entries("placeholder", placeholder, "value", value)));
                return Map.of("expression", operand(placeholder, path) + " != " + operand(value, path));
            case "EQUALS_IGNORECASE_STRING":
                requireLiteral(literal, path);
                return Map.of("placeholder_match", entries("placeholder", placeholder, "regex", "(?i)" + Pattern.quote(value)));
            case "CONTAINS_STRING":
                requireLiteral(literal, path);
                return Map.of("placeholder_match", entries("placeholder", placeholder, "regex", "(?s).*" + Pattern.quote(value) + ".*"));
            case "EQUAL_TO":
                return numeric(placeholder, "==", value, path);
            case "SUPERIOR":
                return numeric(placeholder, ">", value, path);
            case "SUPERIOR_OR_EQUAL":
                return numeric(placeholder, ">=", value, path);
            case "LOWER":
                return numeric(placeholder, "<", value, path);
            case "LOWER_OR_EQUAL":
                return numeric(placeholder, "<=", value, path);
            default:
                throw new IllegalArgumentException(path + ": unknown placeholder comparison '" + action + "'");
        }
    }

    private Object numeric(String placeholder, String operator, String value, String path) {
        return Map.of("expression", operand(placeholder, path) + " " + operator + " " + operand(value, path));
    }

    private static void requireLiteral(boolean literal, String path) {
        if (!literal) {
            throw new IllegalArgumentException(path + ": this comparison cannot read a quest binding;"
                    + " compare with a fixed value, or with EQUALS_STRING / DIFFERENT_STRING");
        }
    }

    /**
     * One side of an {@code expression}: a binding ({@code {quest_name}} → {@code quest_name}), a
     * single {@code %placeholder%}, a number, or a quoted string. A text mixing them has no
     * expression form.
     */
    private static String operand(String text, String path) {
        Matcher binding = Pattern.compile("\\{([A-Za-z0-9_]+)}").matcher(text);
        if (binding.matches()) return binding.group(1);
        if (SINGLE_PERCENT_TOKEN.matcher(text).matches()) return text;
        if (text.matches("-?\\d+(\\.\\d+)?")) return text;
        if (text.contains("{") || text.contains("%")) {
            throw new IllegalArgumentException(path + ": '" + text + "' mixes a placeholder with text,"
                    + " which an Artisan condition cannot compare");
        }
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    // ------------------------------------------------------------------ texts

    private String text(Map<String, Object> action, String field, String keyField, String path) {
        if (action.get(keyField) != null) {
            return convertText(keyText(string(action.get(keyField)), path));
        }
        String text = string(action.get(field));
        return text != null ? convertText(text) : "";
    }

    private String keyText(@Nullable String key, String path) {
        String text = key != null ? messageKeys.apply(key) : null;
        if (text != null) return text;
        notes.add("Lang key '" + key + "' at " + path + " is unknown: the key itself is shown.");
        return key != null ? key : "";
    }

    /** {@code %quest_name%} → {@code {quest_name}} for the chain's bindings; {@code %msquests_x%} → {@code %quests_x%}. */
    static String convertText(String text) {
        Matcher matcher = PERCENT_TOKEN.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement;
            if (BINDINGS.contains(name)) {
                replacement = "{" + name + "}";
            } else if (name.startsWith("msquests_")) {
                replacement = "%quests_" + name.substring("msquests_".length()) + "%";
            } else {
                replacement = matcher.group();
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    // ----------------------------------------------------------------- values

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private static List<?> list(@Nullable Object value, String path) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) return list;
        throw new IllegalArgumentException(path + ": expected a list, got " + value);
    }

    private static String required(Map<String, Object> map, String field, String path) {
        String value = string(map.get(field));
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(path + ": '" + field + "' is required");
        }
        return value;
    }

    private static @Nullable String string(@Nullable Object value) {
        return value != null ? value.toString() : null;
    }

    private static @Nullable String lower(@Nullable Object value) {
        return value != null ? value.toString().toLowerCase(Locale.ROOT) : null;
    }

    private static Map<String, Object> entries(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    private static void putIfSet(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) map.put(key, value);
    }
}
