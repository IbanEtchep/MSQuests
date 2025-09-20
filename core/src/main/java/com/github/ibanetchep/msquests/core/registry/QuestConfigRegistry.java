package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestConfigRegistry {

    private final Map<String, QuestGroupConfig> questGroupConfigs = new ConcurrentHashMap<>();

    public Map<String, QuestGroupConfig> getQuestGroupConfigs() {
        return Collections.unmodifiableMap(questGroupConfigs);
    }

    public void clearQuestGroupConfigs() {
        questGroupConfigs.clear();
    }

    public void registerQuestGroupConfig(QuestGroupConfig questGroupConfig) {
        questGroupConfigs.put(questGroupConfig.getKey(), questGroupConfig);
    }
}
