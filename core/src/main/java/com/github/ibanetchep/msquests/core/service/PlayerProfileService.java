package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.cache.PlayerProfileCache;
import com.github.ibanetchep.msquests.core.cache.QuestActorCache;
import com.github.ibanetchep.msquests.core.repository.PlayerProfileRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerProfileService {

    private final Logger logger;
    private final PlayerProfileRepository repository;
    private final PlayerProfileCache playerProfileCache;
    private final QuestActorCache questActorCache;

    public PlayerProfileService(
            Logger logger,
            PlayerProfileRepository repository,
            PlayerProfileCache playerProfileCache,
            QuestActorCache questActorCache
    ) {
        this.logger = logger;
        this.repository = repository;
        this.playerProfileCache = playerProfileCache;
        this.questActorCache = questActorCache;
    }

    public CompletableFuture<PlayerProfile> loadProfile(UUID id) {
        return repository.get(id).thenApply(dto -> {
            if (dto == null) {
                dto = new PlayerProfileDTO(id, null);
                repository.save(dto);
            }

            PlayerProfile profile = new PlayerProfile(id);
            profile.setTrackedQuestId(dto.trackedQuestId());

            playerProfileCache.registerPlayerProfile(profile);
            questActorCache.linkProfileToActors(profile);
            return profile;
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load profile " + id, e);
            return null;
        });
    }

    public CompletableFuture<Void> saveProfile(PlayerProfile profile) {
        return repository.save(new PlayerProfileDTO(profile.getId(), profile.getTrackedQuestId()))
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to save profile " + profile.getId(), e);
                    return null;
                });
    }
}
