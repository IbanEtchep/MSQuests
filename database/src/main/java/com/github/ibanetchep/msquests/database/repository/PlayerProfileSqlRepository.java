package com.github.ibanetchep.msquests.database.repository;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;
import com.github.ibanetchep.msquests.database.DbAccess;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerProfileSqlRepository extends SqlRepository implements PlayerProfileRepository {

    public PlayerProfileSqlRepository(DbAccess dbAccess) {
        super(dbAccess);
    }


    @Override
    public CompletableFuture<PlayerProfileDTO> get(UUID id) {
        String query = "SELECT * FROM msquests_player_profile WHERE id = :id";

        return supplyAsync(() -> getJdbi().withHandle(handle -> handle.createQuery(query)
                .bind("id", id.toString())
                .mapTo(PlayerProfileDTO.class)
                .findFirst()
                .orElse(null)));
    }

    @Override
    public CompletableFuture<Void> save(PlayerProfileDTO quest) {
        return runAsync(() -> getJdbi().useTransaction(handle -> {
            String id = quest.id().toString();
            String trackedQuestId = quest.trackedQuestId() != null ? quest.trackedQuestId().toString() : null;
            
            int updated = handle.createUpdate(
                    "UPDATE msquests_player_profile SET tracked_quest_id = :trackedQuestId WHERE id = :id")
                    .bind("id", id)
                    .bind("trackedQuestId", trackedQuestId)
                    .execute();
            
            if (updated == 0) {
                handle.createUpdate(
                        "INSERT INTO msquests_player_profile (id, tracked_quest_id) VALUES (:id, :trackedQuestId)")
                        .bind("id", id)
                        .bind("trackedQuestId", trackedQuestId)
                        .execute();
            }
        }));
    }
}
