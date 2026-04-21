package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.quest.actor.BukkitQuestPlayerActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.service.PlayerProfileService;
import com.github.ibanetchep.msquests.core.service.QuestActorService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class QuestPlayerService {

    private final QuestActorService questActorService;
    private final PlayerProfileService playerProfileService;

    public QuestPlayerService(QuestActorService questActorService, PlayerProfileService playerProfileService) {
        this.questActorService = questActorService;
        this.playerProfileService = playerProfileService;
    }

    public CompletableFuture<PlayerProfile> loadPlayer(Player player) {
        BukkitQuestPlayerActor actor = new BukkitQuestPlayerActor(player.getUniqueId(), player.getName());
        return questActorService.loadActor(actor)
                .thenCompose(v -> playerProfileService.loadProfile(player.getUniqueId(), player.getName()));
    }

    public CompletableFuture<Void> unloadPlayer(Player player) {
        return playerProfileService.unloadProfile(player.getUniqueId())
                .thenRun(() -> questActorService.unloadActor(player.getUniqueId()));
    }

    public CompletableFuture<Void> loadAllPlayers() {
        CompletableFuture<?>[] futures = Bukkit.getOnlinePlayers().stream()
                .map(this::loadPlayer)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }
}
