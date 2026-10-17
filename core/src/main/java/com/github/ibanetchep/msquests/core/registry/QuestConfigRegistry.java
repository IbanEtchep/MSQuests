package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.jetbrains.annotations.Nullable;

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

    /** Quests of {@code quest_group} Documents by uid, swapped together with the groups. */
    private volatile Map<String, QuestConfig> questsByUid = Map.of();

    public Map<String, QuestGroupConfig> getQuestGroupConfigs() {
        return Collections.unmodifiableMap(questGroupConfigs);
    }

    /** Replaces every group at once. */
    public void replaceQuestGroupConfigs(Collection<QuestGroupConfig> groups) {
        Map<String, QuestGroupConfig> next = new ConcurrentHashMap<>();
        Map<String, QuestConfig> nextByUid = new ConcurrentHashMap<>();
        for (QuestGroupConfig group : groups) {
            next.put(group.getKey(), group);
            indexUids(group, nextByUid);
        }
        questsByUid = nextByUid;
        questGroupConfigs = next;
    }

    /** The quest a stored row references by uid, or null when no loaded Document holds it. */
    public @Nullable QuestConfig getQuestByUid(String uid) {
        return questsByUid.get(uid);
    }

    private static void indexUids(QuestGroupConfig group, Map<String, QuestConfig> index) {
        for (QuestConfig quest : group.getOrderedQuests()) {
            if (quest.getUid() != null) {
                index.put(quest.getUid(), quest);
            }
        }
    }

    public void registerQuestGroupConfig(QuestGroupConfig questGroupConfig) {
        questGroupConfigs.put(questGroupConfig.getKey(), questGroupConfig);
        Map<String, QuestConfig> nextByUid = new ConcurrentHashMap<>(questsByUid);
        indexUids(questGroupConfig, nextByUid);
        questsByUid = nextByUid;
    }
}
