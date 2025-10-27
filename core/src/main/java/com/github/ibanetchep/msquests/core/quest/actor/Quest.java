package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.util.CronUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class Quest implements PlaceholderProvider {

    private final UUID id;
    private QuestConfig questConfig;
    private QuestStatus status;
    private QuestActor actor;
    private final Map<String, QuestStage> stages = new LinkedHashMap<>();
    private @Nullable Date completedAt;
    private Date createdAt;
    private Date updatedAt;

    public Quest(UUID uniqueId, QuestConfig quest, QuestActor actor, QuestStatus status, @Nullable Date completedAt, Date createdAt, Date updatedAt) {
        this.id = Objects.requireNonNull(uniqueId);
        this.questConfig = Objects.requireNonNull(quest);
        this.status = Objects.requireNonNull(status);
        this.actor = Objects.requireNonNull(actor);
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

    public List<QuestStage> getStagesList() {
        return new ArrayList<>(stages.values());
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
        int index = 0;

        for(QuestStage stage : stages.values()) {
            if(stage.isCompleted()) {
                index++;
            }
        }

        return index;
    }

    public QuestGroupConfig getQuestGroup() {
        return questConfig.getGroupConfig();
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

    public double getProgressRatio() {
        return stages.values().stream()
                .mapToDouble(QuestStage::getProgressRatio)
                .average()
                .orElse(0.0) / 100.0;
    }

    public List<QuestObjective> getObjectives() {
        List<QuestObjective> objectives = new ArrayList<>();
        stages.values().forEach(stage -> objectives.addAll(stage.getObjectives().values()));
        return objectives;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("quest_id", id.toString());
        placeholders.put("quest_key", questConfig.getKey());
        placeholders.put("quest_status", translator.getRaw(status));
        placeholders.put("quest_actor_name", actor.getName());
        placeholders.put("quest_actor_type", actor.getActorType());
        placeholders.put("quest_name", questConfig.getName());
        placeholders.put("quest_description", questConfig.getDescription() != null ? questConfig.getDescription() : "");
        placeholders.put("quest_stage_index", getCurrentStageIndex() + "");
        placeholders.put("quest_stage_count", getStages().size() + "");
        placeholders.put("quest_status_prefix", translator.getRaw(status.getPrefixTranslationKey()));
        placeholders.put("quest_status_suffix", translator.getRaw(status.getSuffixTranslationKey()));

        return placeholders;
    }
}
