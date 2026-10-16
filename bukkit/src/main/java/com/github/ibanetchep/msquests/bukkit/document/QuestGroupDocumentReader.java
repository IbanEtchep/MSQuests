package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.core.dto.ConditionConfigDTO;
import com.github.ibanetchep.msquests.core.dto.DistributionConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
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
 * <p>Actions ({@code on.*}, {@code rewards}) and conditions ({@code requirements}, objective
 * {@code conditions}) are still in the MSQuests format ({@code type: command}, …): they become
 * Artisan chains in a later lot, which will read both forms (a MSQuests step has a {@code type}).
 *
 * <p>Anything malformed is refused with the path of the faulty value; nothing is guessed.
 */
public final class QuestGroupDocumentReader {

    public static final String SCHEMA = "quest_group/v1";

    /** {@code on.<hook>} → the hook of {@link QuestGroupConfigActionsDTO}. */
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

        Map<String, Object> on = map(doc.get("on"), "on");
        for (String hook : on.keySet()) {
            if (!HOOKS.contains(hook)) {
                throw new DocumentException("on." + hook, "unknown hook; expected one of " + HOOKS);
            }
        }
        QuestGroupConfigActionsDTO actions = new QuestGroupConfigActionsDTO(
                actions(on.get("quest_start"), "on.quest_start"),
                actions(on.get("quest_complete"), "on.quest_complete"),
                actions(on.get("objective_progress"), "on.objective_progress"),
                actions(on.get("objective_complete"), "on.objective_complete"),
                actions(on.get("distribution"), "on.distribution"),
                actions(on.get("actor_load"), "on.actor_load"),
                actions(on.get("period_complete"), "on.period_complete"));

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

        try {
            return new QuestConfigDTO(
                    key,
                    Ltext.resolve(quest.get("title"), lang, path + ".title"),
                    Ltext.resolve(quest.get("description"), lang, path + ".description"),
                    QuestDuration.parse(quest.get("duration"), path + ".duration"),
                    string(quest.get("tier"), path + ".tier"),
                    actions(quest.get("rewards"), path + ".rewards"),
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

    private static List<QuestActionDTO> actions(@Nullable Object value, String path) {
        List<QuestActionDTO> actions = new ArrayList<>();
        List<Object> items = list(value, path);
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> action = map(items.get(i), path + "[" + i + "]");
            requiredString(action, "type", path + "[" + i + "]");
            actions.add(DocumentYaml.fromTree(action, QuestActionDTO.class));
        }
        return actions;
    }

    private static List<ConditionConfigDTO> conditions(Object value, String path) {
        List<ConditionConfigDTO> conditions = new ArrayList<>();
        List<Object> items = list(value, path);
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> condition = map(items.get(i), path + "[" + i + "]");
            requiredString(condition, "type", path + "[" + i + "]");
            conditions.add(DocumentYaml.fromTree(condition, ConditionConfigDTO.class));
        }
        return conditions;
    }

    // ------------------------------------------------------------------ values

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
