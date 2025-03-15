package com.github.ibanetchep.msquests.manager;

import com.github.ibanetchep.msquests.MSQuestsPlugin;
import com.github.ibanetchep.msquests.database.repository.QuestEntryRepository;
import com.github.ibanetchep.msquests.model.Quest;
import com.github.ibanetchep.msquests.model.QuestEntry;
import com.github.ibanetchep.msquests.model.actor.QuestActor;

import java.util.Map;
import java.util.UUID;

public class QuestEntryManager extends AbstractManager<UUID, QuestEntry> {

    private final QuestEntryRepository questEntryRepository;

    public QuestEntryManager(MSQuestsPlugin plugin, QuestEntryRepository questEntryRepository) {
        super(plugin);
        this.questEntryRepository = questEntryRepository;
    }

    public void loadQuests(QuestActor actor) {
        Map<UUID, QuestEntry> questEntries = questEntryRepository.getAllByActor(actor.getUniqueId());
    }

    public void startQuest(QuestActor actor, Quest quest) {
    }

    @Override
    public QuestEntry loadFromDatabase(UUID uniqueId) {
        return null;
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
