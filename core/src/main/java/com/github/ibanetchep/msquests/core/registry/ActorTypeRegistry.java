package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestGlobalActor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActorTypeRegistry {

    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();

    public ActorTypeRegistry() {
        registerDefaultActorTypes();
    }

    /**
     * Registers a new actor type.
     *
     * @param key the key of the actor type.
     * @param actorClass the class type of the actor.
     */
    public void registerActorType(String key, Class<? extends QuestActor> actorClass) {
        actorTypes.put(key, actorClass);
    }

    /**
     * Retrieves an actor type by its key.
     *
     * @param key the key of the actor type.
     * @return the class type of the actor, or null if not found.
     */
    public Class<? extends QuestActor> getActorType(String key) {
        return actorTypes.get(key);
    }

    /**
     * Checks if an actor type is registered.
     *
     * @param key the key of the actor type.
     * @return true if the actor type is registered, false otherwise.
     */
    public boolean isActorTypeRegistered(String key) {
        return actorTypes.containsKey(key);
    }

    /**
     * Retrieves all registered actor types.
     *
     * @return a map of all registered actor types with their class types.
     */
    public Map<String, Class<? extends QuestActor>> getAllActorTypes() {
        return Map.copyOf(actorTypes);
    }

    private void registerDefaultActorTypes() {
        registerActorType("global", QuestGlobalActor.class);
    }
}