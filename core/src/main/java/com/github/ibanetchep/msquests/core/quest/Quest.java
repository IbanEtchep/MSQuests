package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Quest {

    private final UUID id;
    private QuestConfig questConfig;
    private QuestStatus status;
    private QuestActor actor;
    private Map<UUID, QuestObjective<?>> objectives;
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

    public Quest(QuestConfig questConfig, QuestActor actor) {
        this(UUID.randomUUID(), questConfig, actor, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
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

    public Map<UUID, QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public void addObjective(QuestObjective<?> objective) {
        this.objectives.put(objective.getId(), objective);
    }

    public QuestObjective<?> getObjective(UUID id) {
        return this.objectives.get(id);
    }
}
