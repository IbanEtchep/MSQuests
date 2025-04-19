package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveHandler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registry for managing objective types and their associated classes.
 * Provides functionality to register objective types and instantiate objectives.
 */
public class ObjectiveTypeRegistry {

    private final Map<String, ObjectiveTypeEntry> registeredTypes = new HashMap<>();
    private final Map<String, QuestObjectiveHandler<?>> handlers = new HashMap<>();

    public ObjectiveTypeRegistry() {
    }

    /**
     * Registers a new objective type with its associated config and handler.
     *
     * @param configClass the objective config class
     * @param objectiveClass the corresponding objective class
     * @param handler the handler for this objective type
     * @throws IllegalArgumentException if the config class is not annotated with ObjectiveType
     */
    public <D extends QuestObjectiveConfig, O extends QuestObjective<D>> void registerType(
            Class<D> configClass,
            Class<O> objectiveClass,
            QuestObjectiveHandler<O> handler
    ) {
        ObjectiveType annotation = configClass.getAnnotation(ObjectiveType.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + configClass.getName() + " is not annotated with ObjectiveConfigType.");
        }

        String typeName = annotation.type();
        registeredTypes.put(typeName, new ObjectiveTypeEntry(configClass, objectiveClass));
        handlers.put(typeName, handler);
    }


    /**
     * Gets the objective config class by its type name.
     *
     * @param name the objective type name
     * @return the objective config class, or null if not found
     */
    public Class<? extends QuestObjectiveConfig> getConfigClass(String name) {
        ObjectiveTypeEntry entry = registeredTypes.get(name);
        return entry != null ? entry.configClass() : null;
    }

    /**
     * Gets the objective class by its type name.
     *
     * @param name the objective type name
     * @return the objective class, or null if not found
     */
    public Class<? extends QuestObjective<?>> getObjectiveClass(String name) {
        ObjectiveTypeEntry entry = registeredTypes.get(name);
        return entry != null ? entry.objectiveClass() : null;
    }

    /**
     * Creates a new objective config instance from configuration.
     *
     * @param type the objective type name
     * @param config the configuration map for the objective
     * @return the created objective config, or null if the type is not registered
     */
    @SuppressWarnings("unchecked")
    public QuestObjectiveConfig createConfig(String type, Map<String, String> config) {
        try {
            Class<? extends QuestObjectiveConfig> configClass = getConfigClass(type);
            if (configClass == null) {
                return null;
            }

            Constructor<? extends QuestObjectiveConfig> constructor = configClass.getConstructor(Map.class);
            return constructor.newInstance(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objective config of type " + type, e);
        }
    }

    /**
     * Creates a new objective instance.
     *
     * @param id the objective UUID
     * @param quest the quest this objective belongs to
     * @param progress the initial progress value
     * @param config the objective config
     * @return the created objective instance
     */
    @SuppressWarnings("unchecked")
    public <D extends QuestObjectiveConfig> QuestObjective<D> createObjective(
            UUID id,
            Quest quest,
            int progress,
            D config
    ) {
        try {
            String type = getTypeFromConfig(config);
            if (type == null) {
                throw new IllegalArgumentException("Configuration class is not registered: " + config.getClass().getName());
            }

            Class<? extends QuestObjective<?>> objectiveClass = getObjectiveClass(type);
            Constructor<?> constructor = objectiveClass.getConstructor(UUID.class, Quest.class, int.class, config.getClass());

            return (QuestObjective<D>) constructor.newInstance(id, quest, progress, config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objective instance for config: " + config.getClass().getName(), e);
        }
    }

    /**
     * Gets the type name from a config instance.
     *
     * @param config the objective config
     * @return the type name, or null if not found
     */
    private String getTypeFromConfig(QuestObjectiveConfig config) {
        Class<?> configClass = config.getClass();
        ObjectiveType annotation = configClass.getAnnotation(ObjectiveType.class);

        if (annotation != null) {
            String typeName = annotation.type();
            if (registeredTypes.containsKey(typeName)) {
                return typeName;
            }
        }

        // Check if any registered class is assignable from the config class
        for (Map.Entry<String, ObjectiveTypeEntry> entry : registeredTypes.entrySet()) {
            if (entry.getValue().configClass().isAssignableFrom(configClass)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Gets all registered objective types.
     *
     * @return a map of all registered objective types with their entries
     */
    public Map<String, ObjectiveTypeEntry> getRegisteredTypes() {
        return Map.copyOf(registeredTypes);
    }

    /**
     * Internal class representing an entry in the objective type registry.
     * Contains both the config class and the objective class.
     */
    public record ObjectiveTypeEntry(
            Class<? extends QuestObjectiveConfig> configClass,
            Class<? extends QuestObjective<?>> objectiveClass
    ) {}
}