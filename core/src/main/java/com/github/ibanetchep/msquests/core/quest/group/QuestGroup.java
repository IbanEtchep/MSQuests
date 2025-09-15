package com.github.ibanetchep.msquests.core.quest.group;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class QuestGroup {

    private final String key;
    private final String name;
    private final String description;

    private final Map<String, QuestConfig> questConfigs = new ConcurrentHashMap<>();
    private final List<QuestConfig> orderedQuests = new CopyOnWriteArrayList<>();

    private final Instant startAt;
    private final Instant endAt;

    public QuestGroup(String key, String name, String description, Instant startAt, Instant endAt) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public abstract QuestGroupType getType();

    public abstract QuestConfig getNextQuest(QuestConfig current);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, QuestConfig> getQuestConfigs() {
        return Collections.unmodifiableMap(questConfigs);
    }

    public void addQuest(QuestConfig quest) {
        questConfigs.put(quest.getKey(), quest);
        orderedQuests.add(quest);
        quest.setGroup(this);
    }

    public void removeQuest(QuestConfig quest) {
        questConfigs.remove(quest.getKey());
        orderedQuests.remove(quest);
    }

    public String getKey() {
        return key;
    }

    public List<QuestConfig> getOrderedQuests() {
        return Collections.unmodifiableList(orderedQuests);
    }

    public boolean isActive() {
        return startAt == null || startAt.isBefore(Instant.now()) && endAt == null || endAt.isAfter(Instant.now());
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

}
