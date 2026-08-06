package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActorResolverTest {

    private final UUID playerId = UUID.randomUUID();
    private QuestActorRegistry registry;
    private ActorResolver resolver;
    private TestActor playerActor;
    private TestActor globalActor;

    @BeforeEach
    void setUp() {
        registry = new QuestActorRegistry();
        resolver = new ActorResolver(registry);
        playerActor = new TestActor(playerId, "Steve", "player");
        globalActor = new TestActor(UUID.randomUUID(), "Server", "global");
        registry.registerActor(playerActor);
        registry.registerActor(globalActor);
    }

    @Test
    void resolvesThePlayerActorByDefault() {
        assertEquals(playerActor, resolver.resolve(playerId, null));
        assertEquals(playerActor, resolver.resolve(playerId, ""));
        assertEquals(playerActor, resolver.resolve(playerId, "player"));
    }

    @Test
    void actorTypeIsCaseInsensitive() {
        assertEquals(globalActor, resolver.resolve(playerId, "GLOBAL"));
    }

    @Test
    void resolvesTheGlobalActorForAnyPlayer() {
        assertEquals(globalActor, resolver.resolve(UUID.randomUUID(), "global"));
    }

    /** Membership, not identity — this is the seam the future guild actor plugs into. */
    @Test
    void resolvesAnyActorTypeByMembership() {
        TestActor guild = new TestActor(UUID.randomUUID(), "Knights", "guild");
        guild.members.add(playerId);
        registry.registerActor(guild);

        assertEquals(guild, resolver.resolve(playerId, "guild"));
        assertNull(resolver.resolve(UUID.randomUUID(), "guild"));
    }

    @Test
    void returnsNullWhenNoPlayerIsKnown() {
        assertNull(resolver.resolve(null, "player"));
        assertNull(resolver.resolve(null, "global"));
    }

    @Test
    void returnsNullForAnUnknownActorType() {
        assertNull(resolver.resolve(playerId, "nope"));
    }

    @Test
    void returnsNullWhenThePlayerActorIsNotLoaded() {
        QuestActor unknown = resolver.resolve(UUID.randomUUID(), "player");
        assertNull(unknown);
    }

    /** Membership is not identity: that is what lets a guild actor resolve. */
    private static final class TestActor extends QuestActor {
        private final String actorType;
        final Set<UUID> members = new HashSet<>();

        TestActor(UUID id, String name, String actorType) {
            super(id, name);
            this.actorType = actorType;
        }

        @Override
        public String getActorType() {
            return actorType;
        }

        @Override
        public boolean isMember(UUID playerId) {
            return switch (actorType) {
                case "global" -> true;
                case "guild" -> members.contains(playerId);
                default -> id.equals(playerId);
            };
        }
    }
}
