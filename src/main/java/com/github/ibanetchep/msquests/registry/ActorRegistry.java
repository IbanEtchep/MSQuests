package com.github.ibanetchep.msquests.registry;

import com.github.ibanetchep.msquests.database.dto.QuestActorDTO;
import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.model.actor.QuestGlobalActor;
import com.github.ibanetchep.msquests.model.actor.QuestPlayerActor;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActorRegistry {

    private final Map<String, Class<? extends QuestActor>> actorTypes = new ConcurrentHashMap<>();

    public ActorRegistry() {
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
        registerActorType("player", QuestPlayerActor.class);
        registerActorType("global", QuestGlobalActor.class);
    }


    /**
     * Creates an actor instance by its type.
     *
     * @param actorDTO the actor data transfer object.
     * @return a new instance of the actor, or null if the type is not registered.
     */
    public QuestActor createActor(QuestActorDTO actorDTO) {
        Class<? extends QuestActor> actorClass = actorTypes.get(actorDTO.actorType());
        if (actorClass == null) {
            return null;
        }

        try {
            Constructor<? extends QuestActor> constructor = actorClass.getConstructor(java.util.UUID.class, String.class);
            return constructor.newInstance(actorDTO.id(), actorDTO.actorReferenceId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}