package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Maps (viewer, actor type) to the {@link QuestActor} whose quests a menu should show.
 *
 * <p>This is what a display surface needs whenever quests are not the viewer's own: the
 * Artisan {@code actor} data-source parameter, a command targeting a guild, a placeholder
 * for a server-wide quest. Deliberately open-ended rather than a
 * {@code player|global} switch: any type registered in the
 * {@link ActorTypeRegistry} resolves through
 * {@link QuestActor#isMember(UUID)}.
 *
 * <p>Resolution is memory-only (loaded actors), because it runs inside a data-source
 * fetcher on the main thread. An actor that is not loaded resolves to null and the row
 * builders degrade to neutral values.
 */
public final class ActorResolver {

    public static final String PLAYER = "player";

    private final QuestActorRegistry registry;

    public ActorResolver(QuestActorRegistry registry) {
        this.registry = registry;
    }

    public @Nullable QuestActor resolve(@Nullable UUID playerId, @Nullable String actorType) {
        if (playerId == null) {
            return null;
        }
        String type = actorType == null || actorType.isBlank()
                ? PLAYER
                : actorType.toLowerCase(Locale.ROOT);

        if (PLAYER.equals(type)) {
            QuestActor actor = registry.getActors().get(playerId);
            return actor != null && PLAYER.equalsIgnoreCase(actor.getActorType()) ? actor : null;
        }

        return registry.getActorsByType(type).stream()
                .filter(actor -> actor.isMember(playerId))
                .findFirst()
                .orElse(null);
    }
}
