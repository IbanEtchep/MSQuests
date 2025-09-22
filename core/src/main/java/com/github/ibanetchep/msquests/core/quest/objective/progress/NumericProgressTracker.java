package com.github.ibanetchep.msquests.core.quest.objective.progress;

import com.google.gson.Gson;

import java.util.Map;

public class NumericProgressTracker implements ProgressTracker {

    private static final Gson GSON = new Gson();

    private int progress;
    private int target;

    public NumericProgressTracker() {}

    public NumericProgressTracker(int progress, int target) {
        this.progress = progress;
        this.target = target;
    }

    @Override
    public double getProgressPercent() {
        if (target == 0) return 0;
        return (double) progress / target * 100;
    }

    @Override
    public boolean isCompleted() {
        return progress >= target;
    }

    @Override
    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public void loadFromJson(String json) {
        NumericProgressTracker tracker = GSON.fromJson(json, NumericProgressTracker.class);
        this.progress = tracker.progress;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return Map.of(
                "progress", String.valueOf(progress),
                "target", String.valueOf(target),
                "progress_percent", getProgressPercent() + "%"
        );
    }

    @Override
    public String getTranslationKey() {
        return "progress_tracker.numeric";
    }

    public void incrementProgress(int amount) {
        this.progress += amount;
    }
}
