package com.github.ibanetchep.msquests.core.repository;

import com.github.ibanetchep.msquests.core.dto.PlayerProfileDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProfileRepository {

    CompletableFuture<PlayerProfileDTO> get(UUID id);

    CompletableFuture<Void> save(PlayerProfileDTO quest);

}