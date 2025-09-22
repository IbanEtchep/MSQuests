package com.github.ibanetchep.msquests.core.quest.objective.progress;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translatable;

public interface ProgressTracker extends Translatable, PlaceholderProvider {

    boolean isCompleted();

    double getProgressPercent();

    String toJson();

    void loadFromJson(String json);

    default double getProgressFraction() {
        return getProgressPercent() / 100;
    }
}
