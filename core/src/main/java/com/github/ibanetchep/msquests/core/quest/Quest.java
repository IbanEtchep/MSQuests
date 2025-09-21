package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.Flow;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Quest {

    private final UUID id;
    private QuestConfig questConfig;
    private QuestStatus status;
    private QuestActor actor;
    private final Map<String, QuestObjective<?>> objectives = new LinkedHashMap<>();
    @Nullable
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;

    public Quest(UUID uniqueId, QuestConfig quest, QuestActor actor, QuestStatus status, @Nullable Date completedAt, Date createdAt, Date updatedAt) {
        this.id = uniqueId;
        this.questConfig = quest;
        this.status = status;
        this.actor = actor;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public QuestActor getActor() {
        return actor;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public @Nullable Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(@Nullable Date completedAt) {
        this.completedAt = completedAt;
    }

    public void setActor(QuestActor actor) {
        this.actor = actor;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return this.status == QuestStatus.IN_PROGRESS;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public void setQuestConfig(QuestConfig questConfig) {
        this.questConfig = questConfig;
    }

    public Map<String, QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public void addObjective(QuestObjective<?> objective) {
        this.objectives.put(objective.getObjectiveConfig().getKey(), objective);
    }

    public QuestObjective<?> getObjective(String key) {
        return this.objectives.get(key);
    }

    public QuestGroupConfig getQuestGroup() {
        return questConfig.getGroup();
    }

    public List<QuestObjective<?>> getActiveObjectives() {
        Flow flow = questConfig.getFlow();

        if (flow == Flow.PARALLEL) {
            return objectives.values().stream()
                    .filter(o -> o.getObjectiveConfig().isValid())
                    .filter(o -> o.getStatus() != QuestObjectiveStatus.COMPLETED)
                    .toList();
        }

        var firstActiveObjective = getFirstActiveObjective();
        if (flow == Flow.SEQUENTIAL && firstActiveObjective != null && firstActiveObjective.getObjectiveConfig().isValid()) {
            return List.of(firstActiveObjective);
        }

        return List.of();
    }

    public @Nullable QuestObjective<?> getFirstActiveObjective() {
        return objectives.values().stream()
                .filter(o -> o.getStatus() != QuestObjectiveStatus.COMPLETED)
                .findFirst().orElse(null);
    }

    public  void handleObjectiveCompletion(QuestObjective<?> objective) {

    }
}
