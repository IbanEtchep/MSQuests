package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.cache.PlayerProfileCache;
import com.github.ibanetchep.msquests.core.cache.QuestActorCache;
import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestActorService {

    private final Logger logger;
    private final QuestActorCache questActorCache;
    private final PlayerProfileCache playerProfileCache;
    private final ActorRepository actorRepository;
    private final QuestService questService;

    public QuestActorService(
            Logger logger,
            ActorRepository actorRepository,
            QuestActorCache questActorCache,
            PlayerProfileCache playerProfileCache,
            QuestService questService
    ) {
        this.logger = logger;
        this.questActorCache = questActorCache;
        this.questService = questService;
        this.playerProfileCache = playerProfileCache;
        this.actorRepository = actorRepository;
    }


    public CompletableFuture<Void> loadActor(QuestActor actor) {
        questActorCache.unregisterActor(actor.getId());

        return actorRepository.get(actor.getId()).thenAccept(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(actor.getActorType(), actor.getId());
                actorRepository.add(actorDTO);
            }

            questActorCache.registerActor(actor);
            questService.loadQuests(actor);
            playerProfileCache.linkActorToProfiles(actor);
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load actor", e);
            return null;
        });
    }

    public CompletableFuture<Void> reloadActors() {
        List<CompletableFuture<Void>> futures = questActorCache.getActors().values().stream()
                .map(this::loadActor)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
