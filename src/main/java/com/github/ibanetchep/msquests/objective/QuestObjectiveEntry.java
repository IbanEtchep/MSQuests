package com.github.ibanetchep.msquests.objective;

import com.github.ibanetchep.msquests.annotation.ConfigurationField;
import com.github.ibanetchep.msquests.model.QuestEntry;
import com.github.ibanetchep.msquests.model.QuestObjectiveStatus;
import com.google.gson.Gson;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestObjectiveEntry {

    protected UUID uniqueId;

    @ConfigurationField
    protected String name;
    @ConfigurationField
    protected String description;

    protected int progress;
    protected QuestObjectiveStatus status = QuestObjectiveStatus.IN_PROGRESS;
    protected QuestEntry quest;
    protected final QuestContributionTracker contributionTracker = new QuestContributionTracker();

    protected Date startedAt;
    protected Date completedAt;
    protected Date createdAt;
    protected Date updatedAt;

    public QuestObjectiveEntry(UUID uniqueId, QuestEntry quest, int progress, Map<String, String> config) {
        this.uniqueId = uniqueId;
        this.name = config.get("name");
        this.description = config.get("description");
        this.quest = quest;
        this.progress = progress;
    }

    public abstract boolean isCompleted();

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(new QuestObjectiveEntrySerialization(this.status, this.progress));
    }

    public void deserialize(String serialized) {
        Gson gson = new Gson();
        QuestObjectiveEntrySerialization serialization = gson.fromJson(serialized, QuestObjectiveEntrySerialization.class);
        this.status = serialization.status();
        this.progress = serialization.progress();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public QuestEntry getQuest() {
        return quest;
    }

    public void callOnProgress() {
        //todo call custom bukkit event
    }

    public void callOnComplete() {
        //todo call custom bukkit event
    }

    public static class QuestContributionTracker {
        private final Map<String, Integer> contributions = new ConcurrentHashMap<>();

        public void incrementContribution(String playerId) {
            contributions.put(playerId, contributions.getOrDefault(playerId, 0) + 1);
        }

        public void addContribution(String playerId, int amount) {
            contributions.put(playerId, contributions.getOrDefault(playerId, 0) + amount);
        }

        public int getContributionCount(String playerId) {
            return contributions.getOrDefault(playerId, 0);
        }

        public Map<String, Integer> getAllContributions() {
            return new ConcurrentHashMap<>(contributions);
        }
    }

    public record QuestObjectiveEntrySerialization(QuestObjectiveStatus status, int progress) {}
}