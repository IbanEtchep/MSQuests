package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveHandler;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ObjectiveTypeRegistry {

    // Stores factory functions to create QuestObjectiveConfig from DTO
    private final Map<String, Function<QuestObjectiveConfigDTO, QuestObjectiveConfig>> registeredTypes = new HashMap<>();

    // Stores handlers for each objective type
    private final Map<String, QuestObjectiveHandler<?>> handlers = new HashMap<>();

    // Stores the actual QuestObjective class for each type
    private final Map<String, Class<? extends QuestObjective<?>>> objectiveClasses = new HashMap<>();

    /**
     * Registers an objective type with a config factory, its objective class, and a handler.
     *
     * @param type The unique objective type key
     * @param configFactory Factory function that converts a DTO into the objective config
     * @param objectiveClass The QuestObjective implementation class
     * @param handler The handler for this objective type
     */
    public <C extends QuestObjectiveConfig, O extends QuestObjective<C>> void registerType(
            String type,
            Function<QuestObjectiveConfigDTO, C> configFactory,
            Class<O> objectiveClass,
            QuestObjectiveHandler<O> handler
    ) {
        registeredTypes.put(type, dto -> configFactory.apply(dto));
        objectiveClasses.put(type, objectiveClass);
        handlers.put(type, handler);
    }

    /**
     * Creates a QuestObjectiveConfig from a DTO using the registered factory.
     *
     * @param dto The DTO containing the config data
     * @return The instantiated QuestObjectiveConfig
     */
    public QuestObjectiveConfig createConfig(QuestObjectiveConfigDTO dto) {
        Function<QuestObjectiveConfigDTO, QuestObjectiveConfig> factory = registeredTypes.get(dto.type());
        if (factory == null) throw new IllegalArgumentException("Unknown objective type: " + dto.type());
        return factory.apply(dto);
    }

    /**
     * Instantiates a QuestObjective and adds it to the given quest.
     *
     * @param quest The quest to attach the objective to
     * @param config The objective configuration
     * @param progress The current progress for the objective
     */
    public void createObjective(Quest quest, QuestObjectiveConfig config, int progress) {
        String type = config.getType();
        Class<? extends QuestObjective<?>> objectiveClass = objectiveClasses.get(type);
        if (objectiveClass == null) throw new IllegalStateException("Objective type not found: " + type);

        try {
            QuestObjective<?> objective = objectiveClass.getConstructor(
                    Quest.class,
                    config.getClass(),
                    int.class
            ).newInstance(quest, config, progress);

            quest.addObjective(objective);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error creating objective of type " + type, e);
        }
    }

    /**
     * Returns an unmodifiable map of all registered handlers.
     */
    public Map<String, QuestObjectiveHandler<?>> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }
}