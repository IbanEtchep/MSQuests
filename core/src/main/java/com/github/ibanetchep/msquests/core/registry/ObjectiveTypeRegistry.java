package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.QuestObjectiveDefinition;
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
     * Registers a new objective type with its associated definition and handler.
     *
     * @param definitionClass the objective definition class
     * @param objectiveClass the corresponding objective class
     * @param handler the handler for this objective type
     * @throws IllegalArgumentException if the definition class is not annotated with ObjectiveDefinitionType
     */
    public <D extends QuestObjectiveDefinition, O extends QuestObjective<D>> void registerType(
            Class<D> definitionClass,
            Class<O> objectiveClass,
            QuestObjectiveHandler<O> handler
    ) {
        ObjectiveType annotation = definitionClass.getAnnotation(ObjectiveType.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + definitionClass.getName() + " is not annotated with ObjectiveDefinitionType.");
        }

        String typeName = annotation.type();
        registeredTypes.put(typeName, new ObjectiveTypeEntry(definitionClass, objectiveClass));
        handlers.put(typeName, handler);
    }


    /**
     * Gets the objective definition class by its type name.
     *
     * @param name the objective type name
     * @return the objective definition class, or null if not found
     */
    public Class<? extends QuestObjectiveDefinition> getDefinitionClass(String name) {
        ObjectiveTypeEntry entry = registeredTypes.get(name);
        return entry != null ? entry.definitionClass() : null;
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
     * Creates a new objective definition instance from configuration.
     *
     * @param type the objective type name
     * @param config the configuration map for the objective
     * @return the created objective definition, or null if the type is not registered
     */
    @SuppressWarnings("unchecked")
    public QuestObjectiveDefinition createDefinition(String type, Map<String, String> config) {
        try {
            Class<? extends QuestObjectiveDefinition> definitionClass = getDefinitionClass(type);
            if (definitionClass == null) {
                return null;
            }

            Constructor<? extends QuestObjectiveDefinition> constructor = definitionClass.getConstructor(Map.class);
            return constructor.newInstance(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objective definition of type " + type, e);
        }
    }

    /**
     * Creates a new objective instance.
     *
     * @param id the objective UUID
     * @param quest the quest this objective belongs to
     * @param progress the initial progress value
     * @param definition the objective definition
     * @return the created objective instance
     */
    @SuppressWarnings("unchecked")
    public <D extends QuestObjectiveDefinition> QuestObjective<D> createObjective(
            UUID id,
            Quest quest,
            int progress,
            D definition
    ) {
        try {
            String type = getTypeFromDefinition(definition);
            if (type == null) {
                throw new IllegalArgumentException("Definition class is not registered: " + definition.getClass().getName());
            }

            Class<? extends QuestObjective<?>> objectiveClass = getObjectiveClass(type);
            Constructor<?> constructor = objectiveClass.getConstructor(UUID.class, Quest.class, int.class, definition.getClass());

            return (QuestObjective<D>) constructor.newInstance(id, quest, progress, definition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objective instance for definition: " + definition.getClass().getName(), e);
        }
    }

    /**
     * Gets the type name from a definition instance.
     *
     * @param definition the objective definition
     * @return the type name, or null if not found
     */
    private String getTypeFromDefinition(QuestObjectiveDefinition definition) {
        Class<?> definitionClass = definition.getClass();
        ObjectiveType annotation = definitionClass.getAnnotation(ObjectiveType.class);

        if (annotation != null) {
            String typeName = annotation.type();
            if (registeredTypes.containsKey(typeName)) {
                return typeName;
            }
        }

        // Check if any registered class is assignable from the definition class
        for (Map.Entry<String, ObjectiveTypeEntry> entry : registeredTypes.entrySet()) {
            if (entry.getValue().definitionClass().isAssignableFrom(definitionClass)) {
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
     * Contains both the definition class and the objective class.
     */
    public record ObjectiveTypeEntry(
            Class<? extends QuestObjectiveDefinition> definitionClass,
            Class<? extends QuestObjective<?>> objectiveClass
    ) {}
}