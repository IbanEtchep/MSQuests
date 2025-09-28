package com.github.ibanetchep.msquests.core.cache;

import com.github.ibanetchep.msquests.core.quest.Quest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestCache {

    private final Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    private final Set<Quest> dirtyQuests = ConcurrentHashMap.newKeySet();

    public void add(Quest quest) {
        quest.getActor().addQuest(quest);
        quests.put(quest.getId(), quest);
    }

    public void remove(Quest quest) {
        quests.remove(quest.getId());
        dirtyQuests.remove(quest);
    }

    public void markDirty(Quest quest) {
        dirtyQuests.add(quest);
    }

    public void clearDirty(Quest quest) {
        dirtyQuests.remove(quest);
    }

    public Collection<Quest> getDirtyQuests() {
        return Collections.unmodifiableSet(dirtyQuests);
    }

    public Collection<Quest> getAllQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    public Quest getQuest(UUID questId) {
        return quests.get(questId);
    }
}
