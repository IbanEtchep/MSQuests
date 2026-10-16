package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;
import com.github.ibanetchep.msquests.database.DbAccess;
import com.github.ibanetchep.msquests.database.SqlTables;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerProfileSqlRepository extends SqlRepository implements PlayerProfileRepository {

    private final SqlTables tables;

    public PlayerProfileSqlRepository(DbAccess dbAccess) {
        this(dbAccess, SqlTables.LEGACY);
    }

    public PlayerProfileSqlRepository(DbAccess dbAccess, SqlTables tables) {
        super(dbAccess);
        this.tables = tables;
    }


    @Override
    public CompletableFuture<PlayerProfileDTO> get(UUID id) {
        String query = "SELECT * FROM " + tables.profile() + " WHERE id = :id";

        return supplyAsync(() -> getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("id", id.toString())
                .mapTo(PlayerProfileDTO.class)
                .findFirst()
                .orElse(null)));
    }

    @Override
    public CompletableFuture<Void> save(PlayerProfileDTO profileDTO) {
        return runAsync(() -> getJdbi().useTransaction(handle -> {
            String id = profileDTO.id().toString();
            String trackedQuestId = profileDTO.trackedQuestId() != null ? profileDTO.trackedQuestId().toString() : null;
            
            int updated = handle.createUpdate(
                    "UPDATE " + tables.profile() + " SET name = :name, tracked_quest_id = :trackedQuestId WHERE id = :id")
                    .bind("id", id)
                    .bind("name", profileDTO.name())
                    .bind("trackedQuestId", trackedQuestId)
                    .execute();
            
            if (updated == 0) {
                handle.createUpdate(
                        "INSERT INTO " + tables.profile() + " (id, name, tracked_quest_id) VALUES (:id, :name, :trackedQuestId)")
                        .bind("id", id)
                        .bind("name", profileDTO.name())
                        .bind("trackedQuestId", trackedQuestId)
                        .execute();
            }
        }));
    }
}
