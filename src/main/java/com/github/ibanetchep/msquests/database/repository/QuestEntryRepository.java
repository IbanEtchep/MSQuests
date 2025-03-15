package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.database.dto.QuestEntryDTO;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class QuestEntryRepository extends Repository<UUID, QuestEntryDTO> {

    public QuestEntryRepository(DbAccess dbAccess) {
        super(dbAccess);
    }

    @Override
    public QuestEntryDTO get(UUID id) {
        return null;
    }

    @Override
    public void add(QuestEntryDTO entity) {

    }

    @Override
    public void update(QuestEntryDTO entity) {

    }

    @Override
    public Map<UUID, QuestEntryDTO> getAll() {
        return Map.of();
    }
}
