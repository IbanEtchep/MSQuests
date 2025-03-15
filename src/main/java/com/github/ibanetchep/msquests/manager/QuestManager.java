package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import com.github.ibanetchep.msquests.model.Quest;

import java.util.Map;
import java.util.UUID;

public class QuestManager extends AbstractManager<UUID, Quest> {

    public QuestManager(MSQuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    public Quest loadFromDatabase(UUID uniqueId) {
        return null;
    }

    @Override
    public Map<UUID, Quest> loadAllFromDatabase() {
        return Map.of();
    }

    @Override
    public void deleteFromDatabase(UUID uniqueId) {

    }

    @Override
    public void saveToDatabase(Quest object) {

    }

    @Override
    public void sync(Quest object) {

    }
}
