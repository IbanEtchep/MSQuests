package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;
import com.github.ibanetchep.msquests.core.quest.condition.Condition;

import java.util.List;

/**
 * Base class for all quest objective configs.
 */
public abstract class QuestObjectiveConfig implements Translatable, PlaceholderProvider {

    protected final String key;
    protected final String type;
    private List<Condition> conditions = List.of();

    protected QuestObjectiveConfig(QuestObjectiveConfigDTO dto) {
        this.key = dto.key();
        this.type = dto.type();
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    /** Convert this params back to a DTO */
    public abstract QuestObjectiveConfigDTO toDTO();

    /**
     * How much progress completes this objective.
     *
     * <p>Declared here rather than left to each concrete config because the target belongs
     * to the configuration, not to a running instance: describing an objective a player has
     * not started yet requires reading it without an instance.
     * {@link com.github.ibanetchep.msquests.core.quest.objective.AbstractQuestObjective}
     * reads it from here, so a catalog view and a live instance can never disagree.
     *
     * <p>Objectives that are simply done-or-not (a command run, a placeholder matched)
     * return 1.
     */
    public abstract int getTarget();

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getTranslationKey() {
        return "objective." + type;
    }
}
