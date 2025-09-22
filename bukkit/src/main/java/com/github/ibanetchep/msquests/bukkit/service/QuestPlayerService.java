package com.github.ibanetchep.msquests.bukkit.service;

import com.github.ibanetchep.msquests.bukkit.quest.actor.QuestPlayerActor;
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
        QuestPlayerActor actor = new QuestPlayerActor(player.getUniqueId(), player.getName());
        return questActorService.loadActor(actor)
                        .thenCompose(v -> playerProfileService.loadProfile(player.getUniqueId()));
    }

    public CompletableFuture<Void> loadAllPlayers() {
        return CompletableFuture.runAsync(() -> {
            Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
        });
    }
}
