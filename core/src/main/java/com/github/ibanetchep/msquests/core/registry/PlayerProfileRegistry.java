package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerProfileRegistry {

    private final Map<UUID, PlayerProfile> playerProfiles = new ConcurrentHashMap<>();

    public PlayerProfile getPlayerProfile(UUID playerId) {
        return playerProfiles.getOrDefault(playerId, new PlayerProfile(playerId));
    }

    public void registerPlayerProfile(PlayerProfile playerProfile) {
        playerProfiles.put(playerProfile.getId(), playerProfile);
    }

    public void unregisterPlayerProfile(UUID playerId) {
        playerProfiles.remove(playerId);
    }

    public void linkActorToProfiles(QuestActor actor) {
        playerProfiles.forEach((uuid, profile) -> {
            if (actor.isMember(uuid)) {
                profile.addActor(actor);
            }
        });
    }
}
