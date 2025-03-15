package com.github.ibanetchep.msquests.registry;

import com.github.ibanetchep.msquests.objective.QuestObjectiveEntry;
import com.github.ibanetchep.msquests.annotation.ObjectiveEntryType;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveEntryTypeRegistry {

    private final Map<String, Class<? extends QuestObjectiveEntry>> registeredObjectiveTypes = new HashMap<>();

    public ObjectiveEntryTypeRegistry() {
    }

    /**
     * Registers a new objective type by extracting the type from the ObjectiveEntryType annotation.
     *
     * @param clazz the class type of the objective.
     * @throws IllegalArgumentException if the class is not annotated with ObjectiveEntryType.
     */
    public void registerObjectiveType(Class<? extends QuestObjectiveEntry> clazz) {
        // Check if the class has the ObjectiveEntryType annotation
        ObjectiveEntryType annotation = clazz.getAnnotation(ObjectiveEntryType.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with ObjectiveEntryType.");
        }

        String typeName = annotation.type();

        registeredObjectiveTypes.put(typeName, clazz);
    }

    /**
     * Retrieves an objective class by its name.
     *
     * @param name the name of the objective.
     * @return the class type of the objective, or null if not found.
     */
    public Class<? extends QuestObjectiveEntry> getObjectiveClass(String name) {
        return registeredObjectiveTypes.get(name);
    }

    /**
     * Retrieves all registered objective names.
     *
     * @return a map of all registered objectives with their class types.
     */
    public Map<String, Class<? extends QuestObjectiveEntry>> getRegisteredObjectiveTypes() {
        return Map.copyOf(registeredObjectiveTypes);
    }
}
