package com.github.ibanetchep.msquests.core.quest.objective;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Map;


public abstract class CounterQuestObjective<C extends QuestObjectiveConfig>  extends AbstractQuestObjective<C, Integer> {

    private final int target;

    public CounterQuestObjective(Quest quest, C objectiveConfig, int progress, int target) {
        super(quest, objectiveConfig, progress);
        this.target = target;
    }

    @Override
    public String progressToJson() {
        return String.valueOf(getProgress());
    }

    @Override
    public void updateProgressFromJson(String json) {
        int newProgress = Integer.parseInt(json);
        updateProgress(progress -> newProgress);
    }

    public int getTarget() {
        return target;
    }

    public void incrementProgress() {
        updateProgress(progress -> progress + 1);
    }

    @Override
    public boolean isCompleted() {
        return getProgress() >= target;
    }

    public double getProgressPercent() {
        return (double) getProgress() / target;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        double percent = getProgressPercent() * 100; // en pourcentage
        String percentStr;

        if (percent == (int) percent) {
            percentStr = String.valueOf((int) percent); // entier
        } else {
            percentStr = String.format("%.1f", percent); // 1 chiffre après la virgule
        }

        return Map.of(
                "objective_progress", String.valueOf(getProgress()),
                "objective_target", String.valueOf(getTarget()),
                "objective_progress_percent", percentStr
        );
    }

}