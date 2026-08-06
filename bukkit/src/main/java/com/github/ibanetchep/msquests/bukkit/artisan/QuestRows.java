package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.core.lang.TranslationKey;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestTierConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.UUID;

/**
 * Builds the plain JSON-shaped maps Artisan data sources hand back.
 *
 * <p>Two rules drive every field here:
 *
 * <ul>
 *   <li><b>Raw over formatted.</b> Presentation belongs to the menu, which composes
 *       {@code {objective_progress}/{objective_target}} and
 *       {@code {rewards.pluck("name").join("
")}} however it wants. Instants are
 *       epoch millis, rendered with {@code until()} — the row carries the fact, not
 *       a view of it (ADR `expression-parity`).</li>
 *   <li><b>Never null.</b> Absent values are {@code ""} / {@code 0} / {@code false}: rows
 *       travel over the WebSocket for editor previews, and a menu binding a missing field
 *       should render blank, not crash.</li>
 * </ul>
 *
 * <p>Per-actor fields degrade to neutral values when no actor is resolved (editor preview,
 * or a group whose actor type the viewer has no actor for). The key-set never varies —
 * that is what lets {@code msquests:groups} and {@code msquests:quests} declare themselves
 * STATIC and be placeable slot by slot in the editor.
 */
public final class QuestRows {

    /** No attempt yet — the value {@code QuestStatus} has no name for. */
    private static final String NOT_STARTED = "NOT_STARTED";

    private QuestRows() {}

    // ------------------------------------------------------------------ groups

    public static Map<String, Object> groupRow(QuestGroupConfig group,
                                               @Nullable ActorQuestGroup actorGroup,
                                               Translator translator) {
        return groupRow(group, actorGroup, null, translator);
    }

    public static Map<String, Object> groupRow(QuestGroupConfig group,
                                               @Nullable ActorQuestGroup actorGroup,
                                               @Nullable Function<QuestConfig, QuestStartResult> startCheck,
                                               Translator translator) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", group.getKey());
        row.put("name", nullToEmpty(group.getName()));
        row.put("description", nullToEmpty(group.getDescription()));
        row.put("actor_type", nullToEmpty(group.getActorType()));
        row.put("max_active", group.getMaxActive());
        row.put("max_per_period", group.getMaxPerPeriod() != null ? group.getMaxPerPeriod() : 0);
        row.put("rotatable", group.isRotatable());

        Instant periodEnd = group.getNextReset() != null ? group.getNextReset() : group.getPeriodEnd();
        row.put("period_end", periodEnd != null ? periodEnd.toEpochMilli() : 0L);

        row.put("active_count", actorGroup != null ? actorGroup.getInProgressCount() : 0);
        row.put("completed_count", actorGroup != null ? actorGroup.getCompletedCount() : 0);
        row.put("can_rotate", actorGroup != null && actorGroup.canRotate());

        List<Map<String, Object>> quests = new ArrayList<>();
        for (QuestConfig config : group.getOrderedQuests()) {
            quests.add(questRow(group, config, actorGroup, startCheck, translator));
        }
        row.put("quests", quests);
        return row;
    }

    // ------------------------------------------------------------------ quests

    public static Map<String, Object> questRow(QuestGroupConfig group,
                                               QuestConfig config,
                                               @Nullable ActorQuestGroup actorGroup,
                                               Translator translator) {
        return questRow(group, config, actorGroup, null, translator);
    }

    /**
     * @param startCheck answers "may this actor start this quest right now" — normally
     *                   {@code QuestDistributionService::canStartQuest} curried on the
     *                   actor. Null when there is no actor (editor preview): startability
     *                   is then unknowable and is not claimed.
     */
    public static Map<String, Object> questRow(QuestGroupConfig group,
                                               QuestConfig config,
                                               @Nullable ActorQuestGroup actorGroup,
                                               @Nullable Function<QuestConfig, QuestStartResult> startCheck,
                                               Translator translator) {
        String questKey = config.getKey();
        // "The actor's current attempt" is answered by the domain (ActorQuestGroup), not
        // re-derived here — quest menus and placeholders ask the same question.
        Quest reference = actorGroup != null ? actorGroup.getCurrentAttempt(questKey) : null;

        Map<String, Object> row = new LinkedHashMap<>();
        // Quest keys are only unique within their group — compose so the Row key of a
        // STATIC source stays unique across the whole server.
        row.put("id", group.getKey() + ":" + questKey);
        row.put("group", group.getKey());
        row.put("key", questKey);
        row.put("name", nullToEmpty(config.getName()));
        row.put("description", nullToEmpty(config.getDescription()));
        row.put("duration", config.getDuration());

        String tier = nullToEmpty(config.getTier());
        QuestTierConfig tierConfig = tier.isEmpty() ? null : group.getTier(tier);
        row.put("tier", tier);
        row.put("tier_name", tierConfig != null ? nullToEmpty(tierConfig.getName()) : "");

        // Status of the player's CURRENT attempt, in the domain's own vocabulary
        // (`QuestStatus`) plus NOT_STARTED for "no attempt". Deliberately not merged with
        // startability below: a rankup quest is NOT_STARTED *and* un-startable at once,
        // and a single field cannot say both.
        // Was the quest handed to this actor for the current period? zMenu called it
        // `assigned_only`; a menu filters on it to show "my 6 daily quests" rather than
        // the whole catalogue. A plain boolean, because Artisan's menu filters compare
        // for equality — `status_key != NOT_STARTED` is not expressible there.
        row.put("assigned", reference != null);
        row.put("status_key", reference != null ? reference.getStatus().name() : NOT_STARTED);
        row.put("status", reference != null
                ? translator.getRaw(reference.getStatus())
                : translator.getRaw(TranslationKey.ZMENU_QUEST_STATUS_AVAILABLE));

        // Whether a new attempt may begin, and what blocks it — the axis a sequential
        // group (rankup: one quest at a time) needs to grey out what is not reachable yet.
        QuestStartResult start = startCheck != null ? startCheck.apply(config) : null;
        row.put("can_start", start != null && start.isSuccess());
        row.put("start_blocker", start == null || start.isSuccess() ? "" : start.name());
        row.put("start_blocker_label", start == null || start.isSuccess()
                ? ""
                : translator.getRaw(start));

        QuestObjective current = reference != null && reference.getCurrentStage() != null
                ? reference.getFirstActiveObjective()
                : null;
        // Raw numbers, not a pre-formatted "34/64": composing the label is the menu's job
        // ({objective_progress}/{objective_target}), and only the menu knows how it should
        // read. Zero when there is no attempt or no objective left to show.
        row.put("objective_name", current != null ? translator.getRaw(current.getObjectiveConfig()) : "");
        row.put("objective_progress", current != null ? current.getProgress() : 0);
        row.put("objective_target", current != null ? current.getTarget() : 0);
        row.put("progress_ratio", reference != null ? reference.getProgressRatio() : 0.0);
        row.put("rewards", rewardRows(config));
        // The whole configured tree, with the player's instance laid over it when there is
        // one. Reading it from the config (not from an instance) is what lets a menu show
        // the objectives and rewards of a quest the player has never started.
        row.put("stages", stageRows(config, reference, translator));
        return row;
    }

    private static List<Map<String, Object>> stageRows(QuestConfig config,
                                                       @Nullable Quest reference,
                                                       Translator translator) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int index = 0;
        for (QuestStageConfig stageConfig : config.getStages().values()) {
            QuestStage stage = reference != null ? reference.getStages().get(stageConfig.getKey()) : null;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", stageConfig.getKey());
            row.put("name", nullToEmpty(stageConfig.getName()));
            row.put("flow", stageConfig.getFlow().name());
            row.put("index", index++);
            row.put("completed", stage != null && stage.isCompleted());
            row.put("active", stage != null && stage.isActive());
            row.put("progress_ratio", stage != null ? stage.getProgressRatio() : 0.0);

            List<Map<String, Object>> objectives = new ArrayList<>();
            for (QuestObjectiveConfig objectiveConfig : stageConfig.getObjectives().values()) {
                QuestObjective objective = stage != null ? stage.getObjective(objectiveConfig.getKey()) : null;
                objectives.add(objectiveRow(objectiveConfig, objective, translator));
            }
            row.put("objectives", objectives);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> rewardRows(QuestConfig config) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QuestAction reward : config.getRewards()) {
            Map<String, Object> row = new LinkedHashMap<>();
            String name = reward.getName();
            row.put("type", reward.getType());
            // An unnamed reward shows its type, never a blank line.
            row.put("name", name != null && !name.isEmpty() ? name : reward.getType());
            rows.add(row);
        }
        return rows;
    }

    // ----------------------------------------------------------------- actives

    public static Map<String, Object> activeRow(Quest quest,
                                                @Nullable UUID trackedQuestId,
                                                Translator translator) {
        QuestConfig config = quest.getQuestConfig();
        QuestGroupConfig group = quest.getQuestGroup();
        QuestStage stage = quest.getCurrentStage();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", quest.getId().toString());
        row.put("quest_key", config.getKey());
        row.put("group", group != null ? group.getKey() : "");
        row.put("name", nullToEmpty(config.getName()));
        row.put("description", nullToEmpty(config.getDescription()));
        row.put("status_key", quest.getStatus().name());
        row.put("status", translator.getRaw(quest.getStatus()));
        row.put("tracked", trackedQuestId != null && trackedQuestId.equals(quest.getId()));
        row.put("stage_index", quest.getCurrentStageIndex());
        row.put("stage_count", quest.getStages().size());
        row.put("stage_name", stage != null ? nullToEmpty(stage.getName()) : "");
        row.put("stage_flow", stage != null ? stage.getStageConfig().getFlow().name() : "");
        row.put("progress_ratio", quest.getProgressRatio());

        List<Map<String, Object>> objectives = new ArrayList<>();
        for (QuestObjective objective : quest.getObjectives()) {
            objectives.add(objectiveRow(objective.getObjectiveConfig(), objective, translator));
        }
        row.put("objectives", objectives);
        // Same tree as the catalog row, so a detail menu binds the same paths either way.
        row.put("stages", stageRows(config, quest, translator));

        Instant expiry = expiryOf(quest);
        row.put("expires_at", expiry != null ? expiry.toEpochMilli() : 0L);
        return row;
    }

    /**
     * One objective, described from its config and — when the player has an instance —
     * filled in with their progress. {@code objective} null means "not started": the target
     * is still known, the progress is simply zero.
     */
    private static Map<String, Object> objectiveRow(QuestObjectiveConfig config,
                                                    @Nullable QuestObjective objective,
                                                    Translator translator) {
        int target = config.getTarget();
        int progress = objective != null ? objective.getProgress() : 0;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", config.getKey());
        row.put("name", translator.getRaw(config));
        row.put("type", config.getType());
        row.put("progress", progress);
        row.put("target", target);
        row.put("progress_ratio", objective != null ? objective.getProgressRatio() : 0.0);
        row.put("completed", objective != null && objective.isCompleted());
        row.put("status_key", objective != null
                ? objective.getStatus().name()
                : QuestObjectiveStatus.PENDING.name());
        return row;
    }

    // ------------------------------------------------------------------ shared


    /** Mirrors {@code Quest#shouldExpire()}: the cron period wins, the group end date is the fallback. */
    private static @Nullable Instant expiryOf(Quest quest) {
        QuestGroupConfig group = quest.getQuestGroup();
        if (group == null) return null;
        String cron = group.getResetCron();
        if (cron != null) {
            Instant next = CronUtils.getNextExecution(cron, quest.getCreatedAt().toInstant());
            if (next != null) return next;
        }
        return group.getEndAt();
    }


    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
