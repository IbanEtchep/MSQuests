package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Quest {

    private final UUID id;
    private QuestConfig questConfig;
    private QuestStatus status;
    private QuestActor actor;
    private final Map<String, QuestStage> stages = new LinkedHashMap<>();
    private @Nullable Date completedAt;
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

    public Map<String, QuestStage> getStages() {
        return stages;
    }

    public void addStage(QuestStage stage) {
        stages.put(stage.getKey(), stage);
    }

    public @Nullable QuestStage getCurrentStage() {
        return stages.values().stream()
                .filter(s -> !s.isCompleted())
                .findFirst()
                .orElse(null);
    }

    public int getCurrentStageIndex() {
        for (int i = 0; i < stages.size(); i++) {
            if (!stages.get(i).isCompleted()) {
                return i;
            }
        }

        return stages.size(); // Tous complétés
    }

    public QuestGroupConfig getQuestGroup() {
        return questConfig.getGroup();
    }

    public List<QuestObjective> getActiveObjectives() {
        QuestStage stage = getCurrentStage();

        if (stage == null) {
            return List.of();
        }

        return stage.getActiveObjectives();
    }

    public @Nullable QuestObjective getFirstActiveObjective() {
        if(getCurrentStage() == null) {
            return null;
        }

        return getCurrentStage().getFirstActiveObjective();
    }

    public boolean shouldExpire() {
        if (status != QuestStatus.IN_PROGRESS) {
            return false;
        }

        QuestGroupConfig group = getQuestGroup();
        if (group == null) {
            return false;
        }

        Instant createdAtInstant = getCreatedAt().toInstant();
        Instant now = Instant.now();

        Instant endAt = group.getEndAt();
        if (endAt != null) {
            return now.isAfter(endAt);
        }

        String resetCron = group.getResetCron();
        if (resetCron != null) {
            Instant expiration = CronUtils.getNextExecution(resetCron, createdAtInstant);
            if (expiration != null) {
                return now.isAfter(expiration);
            }
        }

        return false;
    }

    public boolean shouldComplete() {
        return getCurrentStage() == null && getStatus() == QuestStatus.IN_PROGRESS;
    }

    public double getProgressPercent() {
        return stages.values().stream()
                .mapToDouble(QuestStage::getProgressPercent)
                .average()
                .orElse(0.0) / 100.0;
    }

    public List<QuestObjective> getObjectives() {
        List<QuestObjective> objectives = new ArrayList<>();
        stages.values().forEach(stage -> objectives.addAll(stage.getObjectives().values()));
        return objectives;
    }
}
