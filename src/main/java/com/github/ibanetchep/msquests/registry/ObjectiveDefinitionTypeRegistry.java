package com.github.ibanetchep.msquests.registry;

import com.github.ibanetchep.msquests.annotation.ObjectiveDefinitionType;
import com.github.ibanetchep.msquests.model.quest.QuestObjectiveDefinition;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveDefinitionTypeRegistry {

    private final Map<String, Class<? extends QuestObjectiveDefinition>> registeredTypes = new HashMap<>();

    public ObjectiveDefinitionTypeRegistry() {
    }

    /**
     * Enregistre un nouveau type d'objectif en extrayant le type de l'annotation ObjectiveDefinitionType.
     *
     * @param clazz la classe du type d'objectif
     * @throws IllegalArgumentException si la classe n'est pas annotée avec ObjectiveDefinitionType
     */
    public void registerType(Class<? extends QuestObjectiveDefinition> clazz) {
        ObjectiveDefinitionType annotation = clazz.getAnnotation(ObjectiveDefinitionType.class);
        if (annotation == null) {
            throw new IllegalArgumentException("La classe " + clazz.getName() + " n'est pas annotée avec ObjectiveDefinitionType.");
        }

        String typeName = annotation.type();
        registeredTypes.put(typeName, clazz);
    }

    /**
     * Récupère une classe d'objectif par son nom.
     *
     * @param name le nom du type d'objectif
     * @return la classe du type d'objectif, ou null si non trouvé
     */
    public Class<? extends QuestObjectiveDefinition> getType(String name) {
        return registeredTypes.get(name);
    }

    /**
     * Récupère tous les types d'objectifs enregistrés.
     *
     * @return une map de tous les types d'objectifs enregistrés avec leurs classes
     */
    public Map<String, Class<? extends QuestObjectiveDefinition>> getRegisteredTypes() {
        return Map.copyOf(registeredTypes);
    }
} 