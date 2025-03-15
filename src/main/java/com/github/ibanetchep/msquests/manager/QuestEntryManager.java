package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import com.github.ibanetchep.msquests.model.QuestEntry;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.Quest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestEntryManager extends AbstractManager<QuestEntry> {

    private final Map<UUID, QuestEntry> quests = new ConcurrentHashMap<>();

    public QuestEntryManager(MSQuestsPlugin plugin) {
        super(plugin);
    }

    public void startQuest(QuestActor actor, Quest quest) {
    }

    @Override
    public QuestEntry loadFromDatabase(UUID uniqueId) {
        return null;
    }

    public void loadQuests(QuestActor actor) {

    }

    @Override
    public Map<UUID, QuestEntry> loadAllFromDatabase() {
        return Map.of();
    }

    @Override
    public void deleteFromDatabase(UUID uniqueId) {

    }

    @Override
    public void saveToDatabase(QuestEntry object) {

    }

    @Override
    public void sync(QuestEntry object) {

    }
}
