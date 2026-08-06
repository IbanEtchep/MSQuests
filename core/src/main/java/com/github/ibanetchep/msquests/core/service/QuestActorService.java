package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.repository.ActorRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestActorService {

    private final Logger logger;
    private final QuestActorRegistry questActorRegistry;
    private final PlayerProfileRegistry playerProfileRegistry;
    private final ActorRepository actorRepository;
    private final QuestService questService;
    private final QuestLifecycleService questLifecycleService;
    private final MSQuestsPlatform platform;

    public QuestActorService(
            Logger logger,
            ActorRepository actorRepository,
            QuestActorRegistry questActorRegistry,
            PlayerProfileRegistry playerProfileRegistry,
            QuestService questService,
            QuestLifecycleService questLifecycleService,
            MSQuestsPlatform platform
    ) {
        this.logger = logger;
        this.questActorRegistry = questActorRegistry;
        this.questService = questService;
        this.questLifecycleService = questLifecycleService;
        this.playerProfileRegistry = playerProfileRegistry;
        this.actorRepository = actorRepository;
        this.platform = platform;
    }


    public CompletableFuture<Void> loadActor(QuestActor actor) {
        questActorRegistry.unregisterActor(actor.getId());

        return actorRepository.get(actor.getId()).thenCompose(actorDTO -> {
            if (actorDTO == null) {
                actorDTO = new QuestActorDTO(actor.getActorType(), actor.getId());
                actorRepository.add(actorDTO);
            }

            questActorRegistry.registerActor(actor);

            return questService.loadQuests(actor).thenRun(() -> {
                playerProfileRegistry.syncActorMembership(actor);
                questLifecycleService.expireQuests(actor);
                platform.runSync(() -> questLifecycleService.fireActorLoadActions(actor));
            });
        }).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to load actor", e);
            return null;
        });
    }

    /**
     * Unloads an actor and detaches it from every profile still holding it, so an actor that
     * disappears while its members are online (a disbanded guild) stops exposing its quests.
     */
    public void unloadActor(UUID id) {
        QuestActor actor = questActorRegistry.getActors().get(id);

        if (actor != null) {
            for (PlayerProfile profile : List.copyOf(actor.getProfiles())) {
                profile.removeActor(actor);
            }
        }

        questActorRegistry.unregisterActor(id);
    }

    /**
     * Re-evaluates which loaded profiles belong to the actor. Call after any membership change
     * on a shared actor — nothing else recomputes the link once the actor is loaded.
     */
    public void refreshActorMembership(QuestActor actor) {
        playerProfileRegistry.syncActorMembership(actor);
    }

    public CompletableFuture<Void> reloadActors() {
        List<CompletableFuture<Void>> futures = questActorRegistry.getActors().values().stream()
                .map(this::loadActor)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
