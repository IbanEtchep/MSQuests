package com.github.ibanetchep.msquests.core.quest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestGroup {

    private final String key;
    private final String name;
    private final String description;

    private final Map<String, QuestConfig> questConfigs = new ConcurrentHashMap<>();
    private final List<QuestConfig> orderedQuests = new CopyOnWriteArrayList<>();

    public QuestGroup(String key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description;
    }

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
}
