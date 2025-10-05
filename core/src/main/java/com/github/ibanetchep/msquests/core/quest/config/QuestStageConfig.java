package com.github.ibanetchep.msquests.core.quest.config;

import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestStageConfig {

    private final String key;
    private @Nullable String name;
    private Flow flow;
    private final Map<String, QuestObjectiveConfig> objectives;

    public QuestStageConfig(String key, @Nullable String name, Flow flow) {
        this.key = key;
        this.name = name;
        this.flow = flow;
        this.objectives = new LinkedHashMap<>();
    }

    public String getKey() {
        return key;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public Map<String, QuestObjectiveConfig> getObjectives() {
        return objectives;
    }

    public void addObjective(QuestObjectiveConfig objective) {
        objectives.put(objective.getKey(), objective);
    }

    public Flow getFlow() {
        return flow;
    }

    public void setObjectiveCompletionType(Flow flow) {
        this.flow = flow;
    }
}
