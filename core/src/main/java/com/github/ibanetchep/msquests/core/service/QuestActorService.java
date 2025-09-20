package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestActorService {

    private final Logger logger;
    private final QuestActorRegistry questActorRegistry;
    private final PlayerProfileRegistry playerProfileRegistry;
    private final ActorRepository actorRepository;
    private final QuestService questService;

    public QuestActorService(
            Logger logger,
            ActorRepository actorRepository,
            QuestActorRegistry questActorRegistry,
            PlayerProfileRegistry playerProfileRegistry,
            QuestService questService
    ) {
        this.logger = logger;
        this.questActorRegistry = questActorRegistry;
        this.questService = questService;
        this.playerProfileRegistry = playerProfileRegistry;
        this.actorRepository = actorRepository;
    }


    public CompletableFuture<Void> loadActor(QuestActor actor) {
        questActorRegistry.unregisterActor(actor.getId());

        return actorRepository.get(actor.getId()).thenAccept(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(actor.getActorType(), actor.getId());
                actorRepository.add(actorDTO);
            }

            questActorRegistry.registerActor(actor);
            questService.loadQuests(actor);
            playerProfileRegistry.linkActorToProfiles(actor);
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load actor", e);
            return null;
        });
    }

    public CompletableFuture<Void> reloadActors() {
        List<CompletableFuture<Void>> futures = questActorRegistry.getActors().values().stream()
                .map(this::loadActor)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
