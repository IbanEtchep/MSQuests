package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerProfileRegistry {

    private final Map<UUID, PlayerProfile> playerProfiles = new ConcurrentHashMap<>();

    @Nullable
    public PlayerProfile getPlayerProfile(UUID playerId) {
        return playerProfiles.get(playerId);
    }

    public void registerPlayerProfile(PlayerProfile playerProfile) {
        playerProfiles.put(playerProfile.getId(), playerProfile);
    }

    public void unregisterPlayerProfile(UUID playerId) {
        playerProfiles.remove(playerId);
    }

    /**
     * Reconciles every loaded profile with the actor's current membership: members that are
     * not attached yet get attached, and attached profiles that are no longer members get
     * detached.
     *
     * <p>Idempotent, so it doubles as the initial link when an actor is loaded. Shared actor
     * types whose membership changes at runtime — a guild a player joins or leaves — must call
     * this after every change, since {@link QuestActor#isMember(UUID)} is only consulted here.
     */
    public void syncActorMembership(QuestActor actor) {
        playerProfiles.forEach((uuid, profile) -> {
            boolean member = actor.isMember(uuid);
            boolean attached = profile.getActors().containsKey(actor.getId());

            if (member && !attached) {
                profile.addActor(actor);
            } else if (!member && attached) {
                profile.removeActor(actor);
            }
        });
    }
}
