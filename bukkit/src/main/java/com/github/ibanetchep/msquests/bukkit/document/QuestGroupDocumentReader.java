package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.core.dto.DistributionConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigActionsDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestTierConfigDTO;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads a {@code quest_group/v1} Document into the same {@link QuestGroupConfigDTO} the
 * {@code quests/*.yml} reader produces, so everything after it (mapper, registries, services) is
 * shared. The group key is the Document's id (its file name); the {@code uid}s are what the
 * database stores.
 *
 * <p>Actions ({@code actions.*}; formerly {@code on.*} and {@code rewards}, still read) are Artisan chains and conditions ({@code requirements},
 * objective {@code conditions}) Artisan conditions, in the keyed wire; Artisan checks their
 * vocabulary when it runs them. A step or condition still in the MSQuests format
 * ({@code type: command}, written by the first migration) is converted as it is read
 * ({@link LegacyActions}).
 *
 * <p>Anything malformed is refused with the path of the faulty value; nothing is guessed.
 */
public final class QuestGroupDocumentReader {

    public static final String SCHEMA = "quest_group/v1";

    /** {@code actions.<hook>} → the hook of {@link QuestGroupConfigActionsDTO}. */
    static final List<String> HOOKS = List.of(
            "actor_load", "distribution", "quest_start", "quest_complete",
            "objective_progress", "objective_complete", "period_complete");

    private QuestGroupDocumentReader() {
    }

    /**
     * @param groupKey the Document id
     * @param lang     language the texts are loaded in (the project's {@code default_lang})
     */
    public static QuestGroupConfigDTO read(String groupKey, Map<String, Object> doc, String lang) {
        Object schema = doc.get("schema");
        if (!SCHEMA.equals(schema)) {
            throw new DocumentException("schema", "expected " + SCHEMA + ", got " + schema);
        }
        String uid = requiredString(doc, "uid", "");

        Map<String, Object> window = map(doc.get("window"), "window");
        Map<String, Object> reset = map(doc.get("reset"), "reset");
        Map<String, Object> limits = map(doc.get("limits"), "limits");
        Map<String, Object> rotation = map(doc.get("rotation"), "rotation");
        Map<String, Object> distribution = map(doc.get("distribution"), "distribution");

        String timezone = string(reset.get("timezone"), "reset.timezone");
        if (timezone != null) {
            try {
                ZoneId.of(timezone);
            } catch (DateTimeException e) {
                throw new DocumentException("reset.timezone", "unknown time zone '" + timezone + "'");
            }
        }

        Map<String, QuestTierConfigDTO> tiers = null;
        if (doc.get("tiers") != null) {
            tiers = new LinkedHashMap<>();
            for (Map.Entry<String, Object> tier : map(doc.get("tiers"), "tiers").entrySet()) {
                String path = "tiers." + tier.getKey();
                tiers.put(tier.getKey(), new QuestTierConfigDTO(
                        Ltext.resolve(map(tier.getValue(), path).get("title"), lang, path + ".title")));
            }
        }

        Map<String, Integer> tierDistribution = null;
        if (distribution.get("tiers") != null) {
            tierDistribution = new LinkedHashMap<>();
            for (Map.Entry<String, Object> tier : map(distribution.get("tiers"), "distribution.tiers").entrySet()) {
                tierDistribution.put(tier.getKey(), integer(tier.getValue(), "distribution.tiers." + tier.getKey()));
            }
        }

        DistributionConfigDTO distributionDto = null;
        String strategy = string(distribution.get("strategy"), "distribution.strategy");
        if (strategy != null) {
            distributionDto = new DistributionConfigDTO(
                    strategy.toUpperCase(Locale.ROOT),
                    integer(distribution.get("amount"), "distribution.amount"),
                    strings(distribution.get("triggers"), "distribution.triggers"));
        }

        String hooksKey = actionsKey(doc);
        Map<String, Object> hooks = map(doc.get(hooksKey), hooksKey);
        for (String hook : hooks.keySet()) {
            if (!HOOKS.contains(hook)) {
                throw new DocumentException(hooksKey + "." + hook, "unknown hook; expected one of " + HOOKS);
            }
        }
        QuestGroupConfigActionsDTO actions = new QuestGroupConfigActionsDTO(
                chain(hooks.get("quest_start"), hooksKey + ".quest_start"),
                chain(hooks.get("quest_complete"), hooksKey + ".quest_complete"),
                chain(hooks.get("objective_progress"), hooksKey + ".objective_progress"),
                chain(hooks.get("objective_complete"), hooksKey + ".objective_complete"),
                chain(hooks.get("distribution"), hooksKey + ".distribution"),
                chain(hooks.get("actor_load"), hooksKey + ".actor_load"),
                chain(hooks.get("period_complete"), hooksKey + ".period_complete"));

        List<QuestConfigDTO> quests = new ArrayList<>();
        Set<String> questUids = new HashSet<>();
        List<Object> questItems = list(doc.get("quests"), "quests");
        for (int i = 0; i < questItems.size(); i++) {
            QuestConfigDTO quest = quest(questItems.get(i), "quests[" + i + "]", lang);
            if (quest.uid().equals(uid) || !questUids.add(quest.uid())) {
                throw new DocumentException("quests[" + i + "].uid", "uid " + quest.uid() + " is used twice in this group");
            }
            quests.add(quest);
        }

        try {
            return new QuestGroupConfigDTO(
                    groupKey,
                    Ltext.resolve(doc.get("title"), lang, "title"),
                    Ltext.resolve(doc.get("description"), lang, "description"),
                    quests,
                    integer(limits.get("max_active"), "limits.max_active"),
                    integer(limits.get("max_per_period"), "limits.max_per_period"),
                    string(reset.get("cron"), "reset.cron"),
                    instant(window.get("start"), "window.start"),
                    instant(window.get("end"), "window.end"),
                    requiredString(doc, "actor_type", ""),
                    actions,
                    tiers,
                    tierDistribution,
                    bool(rotation.get("enabled"), "rotation.enabled"),
                    integer(rotation.get("max_per_period"), "rotation.max_per_period"),
                    distributionDto,
                    Ltext.resolve(doc.get("objective_line"), lang, "objective_line"),
                    uid,
                    timezone);
        } catch (IllegalArgumentException e) {
            throw new DocumentException("quests", e.getMessage());
        }
    }

    private static QuestConfigDTO quest(Object item, String path, String lang) {
        Map<String, Object> quest = map(item, path);
        String key = requiredString(quest, "key", path);
        String uid = requiredString(quest, "uid", path);

        List<QuestStageConfigDTO> stages = new ArrayList<>();
        Set<String> objectiveKeys = new HashSet<>();
        List<Object> stageItems = list(quest.get("stages"), path + ".stages");
        for (int s = 0; s < stageItems.size(); s++) {
            String stagePath = path + ".stages[" + s + "]";
            Map<String, Object> stage = map(stageItems.get(s), stagePath);
            String stageKey = requiredString(stage, "key", stagePath);
            String flow = string(stage.get("flow"), stagePath + ".flow");

            List<QuestObjectiveConfigDTO> objectives = new ArrayList<>();
            List<Object> objectiveItems = list(stage.get("objectives"), stagePath + ".objectives");
            for (int o = 0; o < objectiveItems.size(); o++) {
                String objectivePath = stagePath + ".objectives[" + o + "]";
                Map<String, Object> objective = map(objectiveItems.get(o), objectivePath);
                String objectiveKey = requiredString(objective, "key", objectivePath);
                requiredString(objective, "type", objectivePath);
                if (objective.get("conditions") != null) {
                    objective = new LinkedHashMap<>(objective);
                    objective.put("conditions", conditions(objective.get("conditions"), objectivePath + ".conditions"));
                }
                // The database keys progress by objective within the quest: two objectives of a
                // quest sharing a key would share their progress.
                if (!objectiveKeys.add(objectiveKey)) {
                    throw new DocumentException(objectivePath + ".key",
                            "objective key '" + objectiveKey + "' is already used in this quest");
                }
                objectives.add(DocumentYaml.fromTree(objective, QuestObjectiveConfigDTO.class));
            }

            stages.add(new QuestStageConfigDTO(
                    stageKey,
                    Ltext.resolve(stage.get("title"), lang, stagePath + ".title"),
                    flow != null ? flow(flow, stagePath + ".flow") : null,
                    objectives));
        }

        // `rewards` is the former name of actions.complete, still read.
        String questActionsKey = actionsKey(quest);
        Map<String, Object> questActions = map(quest.get(questActionsKey), path + "." + questActionsKey);
        List<Object> onComplete = questActions.get("complete") != null
                ? chain(questActions.get("complete"), path + "." + questActionsKey + ".complete")
                : chain(quest.get("rewards"), path + ".rewards");

        try {
            return new QuestConfigDTO(
                    key,
                    Ltext.resolve(quest.get("title"), lang, path + ".title"),
                    Ltext.resolve(quest.get("description"), lang, path + ".description"),
                    QuestDuration.parse(quest.get("duration"), path + ".duration"),
                    string(quest.get("tier"), path + ".tier"),
                    onComplete,
                    stages,
                    quest.get("requirements") != null ? conditions(quest.get("requirements"), path + ".requirements") : null,
                    uid);
        } catch (IllegalArgumentException e) {
            throw new DocumentException(path, e.getMessage());
        }
    }

    private static Flow flow(String flow, String path) {
        try {
            return Flow.valueOf(flow.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new DocumentException(path, "expected parallel or sequential, got '" + flow + "'");
        }
    }

    /**
     * An action chain: a list of keyed steps ({@code {action: args}}, {@code if}/{@code do}/
     * {@code else}, or an action name alone). Only the shape is checked here; Artisan checks the
     * vocabulary when it runs the chain.
     */
    private static List<Object> chain(@Nullable Object value, String path) {
        List<Object> items = value instanceof Map<?, ?> ? List.of(value) : list(value, path);
        List<Object> steps = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Object step = items.get(i);
            String stepPath = path + "[" + i + "]";
            if (LegacyActions.isLegacy(step)) {
                steps.add(legacy(() -> legacyActions().actions(List.of(step), stepPath).get(0), stepPath));
                continue;
            }
            checkStep(step, stepPath);
            steps.add(step);
        }
        return steps;
    }

    private static void checkStep(@Nullable Object step, String path) {
        if (step instanceof String name && !name.isBlank()) return;
        if (step instanceof Map<?, ?> map) {
            if (map.containsKey("if")) {
                chain(map.get("do"), path + ".do");
                if (map.get("else") != null) chain(map.get("else"), path + ".else");
                return;
            }
            if (map.size() == 1) return;
        }
        throw new DocumentException(path, "expected an action ({name: arguments}) or an if/do/else, got " + step);
    }

    /** Artisan conditions, keyed; MSQuests ones are converted. Shape only, like {@link #chain}. */
    private static List<Object> conditions(Object value, String path) {
        List<Object> items = value instanceof Map<?, ?> ? List.of(value) : list(value, path);
        List<Object> conditions = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Object condition = items.get(i);
            String conditionPath = path + "[" + i + "]";
            if (LegacyActions.isLegacy(condition)) {
                conditions.addAll(legacy(() -> legacyActions().conditions(List.of(condition), conditionPath), conditionPath));
                continue;
            }
            if (!(condition instanceof Map<?, ?> map) || map.size() != 1) {
                throw new DocumentException(conditionPath, "expected a condition ({name: arguments}), got " + condition);
            }
            conditions.add(condition);
        }
        return conditions;
    }

    /** For MSQuests steps a Document may still carry; no lang keys to resolve there, the notes are not kept. */
    private static LegacyActions legacyActions() {
        return new LegacyActions(key -> null);
    }

    private static <T> T legacy(java.util.function.Supplier<T> conversion, String path) {
        try {
            return conversion.get();
        } catch (IllegalArgumentException e) {
            throw new DocumentException(path, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ values

    /** {@code actions}, or {@code on}, its former name, in a file that has only that one. */
    private static String actionsKey(Map<String, Object> node) {
        return node.get("actions") == null && node.get("on") != null ? "on" : "actions";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(@Nullable Object value, String path) {
        if (value == null) return Map.of();
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        throw new DocumentException(path, "expected a mapping, got " + value);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(@Nullable Object value, String path) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) return (List<Object>) list;
        throw new DocumentException(path, "expected a list, got " + value);
    }

    private static String requiredString(Map<String, Object> map, String field, String path) {
        String fieldPath = path.isEmpty() ? field : path + "." + field;
        String value = string(map.get(field), fieldPath);
        if (value == null || value.isBlank()) {
            throw new DocumentException(fieldPath, "is required");
        }
        return value;
    }

    private static @Nullable String string(@Nullable Object value, String path) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return value.toString();
        throw new DocumentException(path, "expected a text, got " + value);
    }

    private static @Nullable Integer integer(@Nullable Object value, String path) {
        if (value == null) return null;
        if (value instanceof Number n && n.doubleValue() == n.intValue()) return n.intValue();
        if (value instanceof String s && s.trim().matches("-?\\d+")) return Integer.parseInt(s.trim());
        throw new DocumentException(path, "expected a whole number, got " + value);
    }

    private static @Nullable Boolean bool(@Nullable Object value, String path) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if ("true".equals(value) || "false".equals(value)) return Boolean.parseBoolean((String) value);
        throw new DocumentException(path, "expected true or false, got " + value);
    }

    private static @Nullable List<String> strings(@Nullable Object value, String path) {
        if (value == null) return null;
        List<String> strings = new ArrayList<>();
        List<Object> items = list(value, path);
        for (int i = 0; i < items.size(); i++) {
            strings.add(string(items.get(i), path + "[" + i + "]"));
        }
        return strings;
    }

    private static @Nullable Instant instant(@Nullable Object value, String path) {
        if (value == null) return null;
        if (value instanceof Date date) return date.toInstant();
        try {
            return Instant.parse(value.toString());
        } catch (DateTimeException e) {
            throw new DocumentException(path, "expected a UTC date-time such as 2024-01-01T00:00:00Z, got " + value);
        }
    }
}
