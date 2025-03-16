package com.github.ibanetchep.msquests.registry;

import com.github.ibanetchep.msquests.annotation.ObjectiveDefinitionType;
import com.github.ibanetchep.msquests.model.quest.QuestObjective;
import com.github.ibanetchep.msquests.model.quest.QuestObjectiveDefinition;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveTypeRegistry {

    private final Map<String, ObjectiveTypeEntry> registeredTypes = new HashMap<>();

    public ObjectiveTypeRegistry() {
    }

    /**
     * Registers a new objective type with its associated definition.
     *
     * @param definitionClass the objective definition class
     * @param objectiveClass the corresponding objective class
     * @throws IllegalArgumentException if the definition class is not annotated with ObjectiveDefinitionType
     */
    public <D extends QuestObjectiveDefinition, O extends QuestObjective<D>> void registerType(
            Class<D> definitionClass,
            Class<O> objectiveClass
    ) {
        ObjectiveDefinitionType annotation = definitionClass.getAnnotation(ObjectiveDefinitionType.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + definitionClass.getName() + " is not annotated with ObjectiveDefinitionType.");
        }

        String typeName = annotation.type();
        registeredTypes.put(typeName, new ObjectiveTypeEntry(definitionClass, objectiveClass));
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