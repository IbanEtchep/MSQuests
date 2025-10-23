package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestStage {

    private final Quest quest;
    private final QuestStageConfig stageConfig;
    private final Map<String, QuestObjective> objectives;

    public QuestStage(Quest quest, QuestStageConfig stageConfig) {
        this.quest = quest;
        this.stageConfig = stageConfig;
        this.objectives = new LinkedHashMap<>();
    }

    public Quest getQuest() {
        return this.quest;
    }

    public QuestStageConfig getStageConfig() {
        return this.stageConfig;
    }

    public void addObjective(QuestObjective objective) {
        this.objectives.put(objective.getObjectiveConfig().getKey(), objective);
    }

    public QuestObjective getObjective(String key) {
        return this.objectives.get(key);
    }

    public boolean isCompleted() {
        return this.objectives.values().stream().allMatch(QuestObjective::isCompleted);
    }

    public List<QuestObjective> getActiveObjectives() {
        Flow flow = stageConfig.getFlow();

        if (flow == Flow.PARALLEL) {
            return objectives.values().stream()
                    .filter(o -> !o.isCompleted())
                    .toList();
        }

        var firstActiveObjective = getFirstActiveObjective();
        if (flow == Flow.SEQUENTIAL && firstActiveObjective != null) {
            return List.of(firstActiveObjective);
        }

        return List.of();
    }

    public @Nullable QuestObjective getFirstActiveObjective() {
        if(quest.getCurrentStage() != this) {
            return null;
        }

        return objectives.values().stream()
                .filter(o -> !o.isCompleted())
                .findFirst().orElse(null);
    }

    public Map<String, QuestObjective> getObjectives() {
        return objectives;
    }

    public String getName() {
        return stageConfig.getName();
    }

    public String getKey() {
        return this.stageConfig.getKey();
    }

    public double getProgressPercent() {
        return objectives.values().stream()
                .mapToDouble(QuestObjective::getProgressPercent)
                .average()
                .orElse(0.0) / 100.0;
    }

    public boolean isActive() {
        return quest.getCurrentStage() == this;
    }
}
