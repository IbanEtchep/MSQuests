package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestActorRegistry {

    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();

    public Map<UUID, QuestActor> getActors() {
        return Collections.unmodifiableMap(actors);
    }

    public Collection<QuestActor> getActorsByType(String actorType) {
        return actors.values().stream()
                .filter(actor -> actor.getActorType().equals(actorType))
                .toList();
    }

    public void unregisterActor(UUID id) {
        actors.remove(id);
    }

    public void registerActor(QuestActor actor) {
        actors.put(actor.getId(), actor);
    }

    public void linkProfileToActors(PlayerProfile profile) {
        actors.values().stream()
                .filter(actor -> actor.isMember(profile.getId()))
                .forEach(profile::addActor);
    }
}
