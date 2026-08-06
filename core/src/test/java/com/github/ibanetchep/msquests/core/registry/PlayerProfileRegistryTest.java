package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Membership of a shared actor (a guild) changes while the server runs, unlike the
 * player and global actors whose membership is fixed for their whole lifetime.
 */
class PlayerProfileRegistryTest {

    private final UUID steveId = UUID.randomUUID();
    private final UUID alexId = UUID.randomUUID();
    private PlayerProfileRegistry registry;
    private PlayerProfile steve;
    private PlayerProfile alex;
    private MembershipActor guild;

    @BeforeEach
    void setUp() {
        registry = new PlayerProfileRegistry();
        steve = new PlayerProfile(steveId, "Steve");
        alex = new PlayerProfile(alexId, "Alex");
        registry.registerPlayerProfile(steve);
        registry.registerPlayerProfile(alex);
        guild = new MembershipActor(UUID.randomUUID(), "Knights");
    }

    @Test
    void attachesTheActorToItsMembersOnly() {
        guild.members.add(steveId);

        registry.syncActorMembership(guild);

        assertTrue(steve.getActors().containsKey(guild.getId()));
        assertFalse(alex.getActors().containsKey(guild.getId()));
        assertTrue(guild.getProfiles().contains(steve));
    }

    @Test
    void attachesAPlayerWhoJoinsAfterTheActorWasLoaded() {
        registry.syncActorMembership(guild);
        assertFalse(steve.getActors().containsKey(guild.getId()));

        guild.members.add(steveId);
        registry.syncActorMembership(guild);

        assertTrue(steve.getActors().containsKey(guild.getId()));
    }

    @Test
    void detachesAPlayerWhoLeaves() {
        guild.members.add(steveId);
        registry.syncActorMembership(guild);

        guild.members.remove(steveId);
        registry.syncActorMembership(guild);

        assertFalse(steve.getActors().containsKey(guild.getId()));
        assertFalse(guild.getProfiles().contains(steve));
    }

    @Test
    void isIdempotent() {
        guild.members.add(steveId);

        registry.syncActorMembership(guild);
        registry.syncActorMembership(guild);
        registry.syncActorMembership(guild);

        assertTrue(steve.getActors().containsKey(guild.getId()));
        assertEquals(1, guild.getProfiles().size());
    }

    /**
     * Membership syncing runs on the async actor-loading thread while objective handlers
     * read the profile's quests on the main thread.
     */
    @Test
    void toleratesMembershipChangesConcurrentWithQuestReads() throws Exception {
        MembershipActor[] actors = new MembershipActor[64];
        for (int i = 0; i < actors.length; i++) {
            actors[i] = new MembershipActor(UUID.randomUUID(), "Guild" + i);
            actors[i].members.add(steveId);
        }

        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread writer = new Thread(() -> {
            await(start);
            for (int round = 0; round < 500; round++) {
                for (MembershipActor actor : actors) {
                    actor.members.add(steveId);
                    registry.syncActorMembership(actor);
                    actor.members.remove(steveId);
                    registry.syncActorMembership(actor);
                }
            }
        });
        Thread reader = new Thread(() -> {
            await(start);
            for (int i = 0; i < 20_000; i++) {
                steve.getQuests();
                steve.getActiveObjectivesByType("block_break");
            }
        });

        writer.setUncaughtExceptionHandler((t, e) -> failure.compareAndSet(null, e));
        reader.setUncaughtExceptionHandler((t, e) -> failure.compareAndSet(null, e));
        writer.start();
        reader.start();
        start.countDown();
        writer.join();
        reader.join();

        assertNull(failure.get(), () -> "concurrent access failed: " + failure.get());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
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
