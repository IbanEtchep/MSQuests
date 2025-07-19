package com.github.ibanetchep.msquests.core.quest;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestGroup {

    private final String key;
    private final String name;
    private final String description;

    private final Map<String, QuestConfig>  quests = new ConcurrentHashMap<>();

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

    public Map<String, QuestConfig> getQuests() {
        return Collections.unmodifiableMap(quests);
    }

    public void addQuest(QuestConfig quest) {
        quests.put(quest.getKey(), quest);
        quest.setGroup(this);
    }

    public void removeQuest(QuestConfig quest) {
        quests.remove(quest.getKey());
    }

    public String getKey() {
        return key;
    }
}
