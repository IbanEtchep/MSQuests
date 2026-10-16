package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestConfigRegistry {

    /**
     * Swapped as a whole on reload, never cleared in place: readers on other threads always see
     * either the complete previous set or the complete new one.
     */
    private volatile Map<String, QuestGroupConfig> questGroupConfigs = new ConcurrentHashMap<>();

    public Map<String, QuestGroupConfig> getQuestGroupConfigs() {
        return Collections.unmodifiableMap(questGroupConfigs);
    }

    /** Replaces every group at once. */
    public void replaceQuestGroupConfigs(Collection<QuestGroupConfig> groups) {
        Map<String, QuestGroupConfig> next = new ConcurrentHashMap<>();
        for (QuestGroupConfig group : groups) {
            next.put(group.getKey(), group);
        }
        questGroupConfigs = next;
    }

    public void registerQuestGroupConfig(QuestGroupConfig questGroupConfig) {
        questGroupConfigs.put(questGroupConfig.getKey(), questGroupConfig);
    }
}
