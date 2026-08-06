package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestActorServiceTest {

    private final UUID steveId = UUID.randomUUID();
    private QuestActorRegistry actorRegistry;
    private PlayerProfileRegistry profileRegistry;
    private QuestActorService service;
    private PlayerProfile steve;
    private MembershipActor guild;

    @BeforeEach
    void setUp() {
        actorRegistry = new QuestActorRegistry();
        profileRegistry = new PlayerProfileRegistry();
        service = new QuestActorService(
                Logger.getLogger(QuestActorServiceTest.class.getName()),
                null, actorRegistry, profileRegistry, null, null, null
        );

        steve = new PlayerProfile(steveId, "Steve");
        profileRegistry.registerPlayerProfile(steve);

        guild = new MembershipActor(UUID.randomUUID(), "Knights");
        guild.members.add(steveId);
        actorRegistry.registerActor(guild);
        profileRegistry.syncActorMembership(guild);
    }

    /**
     * A disbanded guild is unloaded while its members are still online: leaving the actor
     * attached to their profiles would keep its quests visible and progressing.
     */
    @Test
    void unloadingAnActorDetachesItFromOnlineProfiles() {
        assertTrue(steve.getActors().containsKey(guild.getId()));

        service.unloadActor(guild.getId());

        assertFalse(steve.getActors().containsKey(guild.getId()));
        assertTrue(guild.getProfiles().isEmpty());
        assertNull(actorRegistry.getActors().get(guild.getId()));
    }

    @Test
    void unloadingAnUnknownActorIsANoOp() {
        service.unloadActor(UUID.randomUUID());

        assertTrue(steve.getActors().containsKey(guild.getId()));
    }

    @Test
    void refreshingMembershipDetachesAPlayerWhoLeft() {
        guild.members.remove(steveId);

        service.refreshActorMembership(guild);

        assertFalse(steve.getActors().containsKey(guild.getId()));
        assertTrue(actorRegistry.getActors().containsKey(guild.getId()));
    }

    @Test
    void refreshingMembershipAttachesANewMember() {
        PlayerProfile alex = new PlayerProfile(UUID.randomUUID(), "Alex");
        profileRegistry.registerPlayerProfile(alex);
        guild.members.add(alex.getId());

        service.refreshActorMembership(guild);

        assertTrue(alex.getActors().containsKey(guild.getId()));
    }

    private static final class MembershipActor extends QuestActor {
        final Set<UUID> members = new HashSet<>();

        MembershipActor(UUID id, String name) {
            super(id, name);
        }

        @Override
        public String getActorType() {
            return "guild";
        }

        @Override
        public boolean isMember(UUID playerId) {
            return members.contains(playerId);
        }
    }
}
