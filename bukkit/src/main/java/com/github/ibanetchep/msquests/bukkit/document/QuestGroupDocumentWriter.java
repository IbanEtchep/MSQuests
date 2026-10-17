package com.github.ibanetchep.msquests.bukkit.document;

import com.github.ibanetchep.msquests.core.dto.QuestConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigActionsDTO;
import com.github.ibanetchep.msquests.core.dto.QuestGroupConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestStageConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestTierConfigDTO;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Writes a group read from {@code quests/*.yml} as a {@code quest_group/v1} Document: the same
 * content under the Document's field names, texts as {@code {lang: text}}, plus the uids. The
 * inverse of {@link QuestGroupDocumentReader}, which a conversion checks by reading the result
 * back.
 */
public final class QuestGroupDocumentWriter {

    private QuestGroupDocumentWriter() {
    }

    /**
     * @param groupUid  the group's uid
     * @param questUid  uid of each quest, by quest key
     * @param stagesOf  the stages to write for a quest: its own, or a copy whose objective keys
     *                  were made unique within the quest
     * @param timezone  zone the reset cron is read in; the server's, which is what it was read in so far
     */
    public static Map<String, Object> write(QuestGroupConfigDTO group, String groupUid,
                                            Function<String, String> questUid,
                                            Function<QuestConfigDTO, List<QuestStageConfigDTO>> stagesOf,
                                            String lang, @Nullable String timezone) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("schema", QuestGroupDocumentReader.SCHEMA);
        doc.put("uid", groupUid);
        doc.put("title", Ltext.of(group.name(), lang));
        doc.put("description", Ltext.of(group.description(), lang));
        if (group.actorType() != null) doc.put("actor_type", group.actorType().toLowerCase(Locale.ROOT));

        putIfAny(doc, "window", entries("start", instant(group.startAt()), "end", instant(group.endAt())));
        putIfAny(doc, "reset", group.resetCron() != null
                ? entries("cron", group.resetCron(), "timezone", timezone)
                : Map.of());
        putIfAny(doc, "limits", entries("max_active", group.maxActive(), "max_per_period", group.maxPerPeriod()));
        putIfAny(doc, "rotation", entries("enabled", group.rotatable(), "max_per_period", group.maxRotationsPerPeriod()));

        Map<String, Object> distribution = new LinkedHashMap<>();
        if (group.distribution() != null) {
            if (group.distribution().strategy() != null) {
                distribution.put("strategy", group.distribution().strategy().toLowerCase(Locale.ROOT));
            }
            putIfSet(distribution, "amount", group.distribution().amount());
            if (group.distribution().triggers() != null) {
                distribution.put("triggers", group.distribution().triggers().stream()
                        .map(t -> t.toLowerCase(Locale.ROOT)).toList());
            }
        }
        if (group.tierDistribution() != null) {
            distribution.put("tiers", new LinkedHashMap<>(group.tierDistribution()));
        }
        putIfAny(doc, "distribution", distribution);

        if (group.tiers() != null) {
            Map<String, Object> tiers = new LinkedHashMap<>();
            for (Map.Entry<String, QuestTierConfigDTO> tier : group.tiers().entrySet()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                putIfSet(entry, "title", Ltext.of(tier.getValue().name(), lang));
                tiers.put(tier.getKey(), entry);
            }
            doc.put("tiers", tiers);
        }

        putIfSet(doc, "objective_line", Ltext.of(group.objectiveLine(), lang));
        putIfAny(doc, "on", hooks(group.actions()));

        List<Object> quests = new ArrayList<>();
        if (group.quests() != null) {
            for (QuestConfigDTO quest : group.quests()) {
                quests.add(quest(quest, questUid.apply(quest.key()), stagesOf.apply(quest), lang));
            }
        }
        doc.put("quests", quests);
        doc.values().removeIf(v -> v == null);
        return doc;
    }

    private static Map<String, Object> hooks(@Nullable QuestGroupConfigActionsDTO actions) {
        Map<String, Object> on = new LinkedHashMap<>();
        if (actions == null) return on;
        putActions(on, "actor_load", actions.actorLoad());
        putActions(on, "distribution", actions.questDistribution());
        putActions(on, "quest_start", actions.questStart());
        putActions(on, "quest_complete", actions.questComplete());
        putActions(on, "objective_progress", actions.objectiveProgress());
        putActions(on, "objective_complete", actions.objectiveComplete());
        putActions(on, "period_complete", actions.allPeriodQuestsComplete());
        return on;
    }

    private static Map<String, Object> quest(QuestConfigDTO quest, String uid, List<QuestStageConfigDTO> stages, String lang) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("key", quest.key());
        out.put("uid", uid);
        putIfSet(out, "tier", quest.tier());
        putIfSet(out, "title", Ltext.of(quest.name(), lang));
        putIfSet(out, "description", Ltext.of(quest.description(), lang));
        if (quest.duration() != null && quest.duration() > 0) {
            out.put("duration", QuestDuration.format(quest.duration()));
        }
        if (quest.requirements() != null && !quest.requirements().isEmpty()) {
            out.put("requirements", quest.requirements());
        }

        List<Object> stageList = new ArrayList<>();
        for (QuestStageConfigDTO stage : stages) {
            Map<String, Object> stageOut = new LinkedHashMap<>();
            stageOut.put("key", stage.key());
            putIfSet(stageOut, "title", Ltext.of(stage.name(), lang));
            if (stage.flow() != null) stageOut.put("flow", stage.flow().name().toLowerCase(Locale.ROOT));
            List<Object> objectives = new ArrayList<>();
            if (stage.objectives() != null) {
                for (QuestObjectiveConfigDTO objective : stage.objectives()) {
                    objectives.add(DocumentYaml.toTree(objective));
                }
            }
            stageOut.put("objectives", objectives);
            stageList.add(stageOut);
        }
        out.put("stages", stageList);

        if (quest.rewards() != null && !quest.rewards().isEmpty()) {
            out.put("rewards", quest.rewards());
        }
        if (quest.rewardDisplay() != null && !quest.rewardDisplay().isEmpty()) {
            out.put("reward_display", quest.rewardDisplay().stream().map(line -> Ltext.of(line, lang)).toList());
        }
        return out;
    }

    private static void putActions(Map<String, Object> on, String hook, @Nullable List<Object> chain) {
        if (chain != null && !chain.isEmpty()) {
            on.put(hook, chain);
        }
    }

    private static @Nullable String instant(@Nullable java.time.Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    private static Map<String, Object> entries(String k1, @Nullable Object v1, String k2, @Nullable Object v2) {
        Map<String, Object> map = new LinkedHashMap<>();
        putIfSet(map, k1, v1);
        putIfSet(map, k2, v2);
        return map;
    }

    private static void putIfSet(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) map.put(key, value);
    }

    private static void putIfAny(Map<String, Object> map, String key, Map<String, Object> value) {
        if (!value.isEmpty()) map.put(key, value);
    }
}
