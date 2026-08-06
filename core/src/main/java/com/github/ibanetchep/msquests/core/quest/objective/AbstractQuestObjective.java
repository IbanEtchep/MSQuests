package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractQuestObjective<C extends QuestObjectiveConfig> implements QuestObjective {

    protected C objectiveConfig;
    protected final AtomicInteger progress;
    protected final int target;
    protected QuestStage questStage;
    protected boolean completed = false;
    protected boolean failed = false;

    /**
     * The target comes from {@link QuestObjectiveConfig#getTarget()} rather than from a
     * constructor argument: it is configuration, so an instance and a catalog view of the
     * same objective cannot drift apart.
     */
    public AbstractQuestObjective(QuestStage questStage, C objectiveConfig, int progress, QuestObjectiveStatus status) {
        this.questStage = questStage;
        this.objectiveConfig = objectiveConfig;
        this.progress = new AtomicInteger(progress);
        this.target = objectiveConfig.getTarget();
        this.completed = (status == QuestObjectiveStatus.COMPLETED);
        this.failed = (status == QuestObjectiveStatus.FAILED);
    }

    @Override
    public QuestStage getStage() {
        return questStage;
    }

    public int getProgress() {
        return progress.get();
    }

    public void incrementProgress(int amount) {
        progress.updateAndGet(current -> {
            int updated = current + amount;
            return Math.min(updated, target);
        });
    }

    public void setProgress(int progress) {
        this.progress.set(progress);
    }

    @Override
    public QuestObjectiveStatus getStatus() {
        if (failed) {
            return QuestObjectiveStatus.FAILED;
        }

        if (completed) {
            return QuestObjectiveStatus.COMPLETED;
        }

        if (!questStage.isActive()) {
            return QuestObjectiveStatus.PENDING;
        }

        if (questStage.getStageConfig().getFlow() == Flow.SEQUENTIAL) {
            QuestObjective firstActiveObjective = questStage.getFirstActiveObjective();
            return firstActiveObjective == this ? QuestObjectiveStatus.IN_PROGRESS : QuestObjectiveStatus.PENDING;
        }

        return QuestObjectiveStatus.IN_PROGRESS;
    }

    public C getObjectiveConfig() {
        return objectiveConfig;
    }

    @Override
    public void complete() {
        incrementProgress(target - getProgress());
        completed = true;
    }

    @Override
    public boolean isCompleted() {
        return getProgress() >= target;
    }

    /**
     * Share of the target reached, normalised to 0..1.
     *
     * <p>Clamped because consumers feed it straight to {@code BossBar#progress}, which
     * rejects anything outside the range: a target lowered in the config after progress
     * was persisted, or a targetless objective, must not blow up the display.
     */
    public double getProgressRatio() {
        if (target <= 0) {
            return 1.0;
        }
        return Math.clamp((double) getProgress() / target, 0.0, 1.0);
    }

    @Override
    public double getProgressPercent() {
        return getProgressRatio() * 100;
    }

    @Override
    public String getProgressPercentFormatted() {
        double percent = getProgressRatio() * 100;
        String percentStr;

        if (percent == (int) percent) {
            percentStr = String.valueOf((int) percent);
        } else {
            percentStr = String.format("%.1f", percent);
        }

        return percentStr;
    }

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("objective_target", target + "");
        placeholders.put("objective_progress", progress.toString());
        placeholders.put("objective_progress_percent", getProgressPercentFormatted());
        placeholders.put("objective_name", translator.getRaw(objectiveConfig));
        placeholders.put("objective_status", translator.getRaw(getStatus()));
        placeholders.put("objective_status_prefix", translator.getRaw(getStatus().getPrefixTranslationKey()));
        placeholders.put("objective_status_suffix", translator.getRaw(getStatus().getSuffixTranslationKey()));

        return placeholders;
    }

    @Override
    public String getTranslationKey() {
        return "objective.progress.default";
    }

    @Override
    public int getTarget() {
        return target;
    }
}