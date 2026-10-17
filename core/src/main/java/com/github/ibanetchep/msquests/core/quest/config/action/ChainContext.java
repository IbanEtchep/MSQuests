package com.github.ibanetchep.msquests.core.quest.config.action;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a chain runs about: an actor and a group, and for the quest and objective hooks the quest
 * and the objective. {@link #args} turns it into the bindings the chain reads as {@code {name}}.
 *
 * <p>The bindings are the names of the design ({@code quest}, {@code quest_title},
 * {@code objective}, {@code objective_title}, {@code progress}, {@code amount},
 * {@code progress_ratio}, {@code actor}, {@code actor_name}, {@code group}, {@code group_title},
 * {@code objectives}) plus every placeholder MSQuests actions knew ({@code quest_name},
 * {@code objective_progress}, {@code quest_tier_name}…), so a converted action renders exactly
 * what it rendered before.
 */
public record ChainContext(QuestActor actor,
                           QuestGroupConfig group,
                           @Nullable Quest quest,
                           @Nullable QuestObjective objective) {

    public static ChainContext of(QuestActor actor, QuestGroupConfig group) {
        return new ChainContext(actor, group, null, null);
    }

    public static ChainContext of(Quest quest) {
        return new ChainContext(quest.getActor(), quest.getQuestGroup(), quest, null);
    }

    public static ChainContext of(QuestObjective objective) {
        Quest quest = objective.getQuest();
        return new ChainContext(quest.getActor(), quest.getQuestGroup(), quest, objective);
    }

    public Map<String, Object> args(Translator translator) {
        Map<String, Object> args = new LinkedHashMap<>();
        putAll(args, actor, translator);
        args.put("actor", actor.getId().toString());
        args.put("actor_type", actor.getActorType());

        putAll(args, group, translator);
        args.put("group", group.getKey());
        args.put("group_title", nullToEmpty(group.getName()));

        if (quest != null) {
            putAll(args, quest.getQuestConfig(), translator);
            putAll(args, quest, translator);
            args.put("quest", quest.getQuestConfig().getKey());
            args.put("quest_title", nullToEmpty(quest.getQuestConfig().getName()));
            args.put("progress_ratio", quest.getProgressRatio());
            args.put("objectives", objectiveLines(quest, translator));
        }

        if (objective != null) {
            putAll(args, objective.getStage(), translator);
            putAll(args, objective, translator);
            args.put("objective", objective.getObjectiveConfig().getKey());
            args.put("objective_title", nullToEmpty(translator.getRaw(objective.getObjectiveConfig())));
            args.put("progress", objective.getProgress());
            args.put("amount", objective.getTarget());
            args.put("progress_ratio", objective.getProgressRatio());
        }
        return args;
    }

    /** The quest's objectives, each rendered with the group's {@code objective_line}, joined as is. */
    private static String objectiveLines(Quest quest, Translator translator) {
        String line = quest.getQuestGroup().getObjectiveLine();
        StringBuilder out = new StringBuilder();
        for (QuestObjective objective : quest.getObjectives()) {
            String rendered = line;
            for (Map.Entry<String, String> entry : objective.getPlaceholders(translator).entrySet()) {
                rendered = rendered.replace("{" + entry.getKey() + "}", nullToEmpty(entry.getValue()));
            }
            out.append(rendered);
        }
        return out.toString();
    }

    private static void putAll(Map<String, Object> args, @Nullable PlaceholderProvider provider, Translator translator) {
        if (provider == null) return;
        for (Map.Entry<String, String> entry : provider.getPlaceholders(translator).entrySet()) {
            args.put(entry.getKey(), nullToEmpty(entry.getValue()));
        }
    }

    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
