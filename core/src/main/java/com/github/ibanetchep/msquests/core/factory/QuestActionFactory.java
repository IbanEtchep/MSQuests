package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestActionDTO;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ActionType;
import com.github.ibanetchep.msquests.core.util.JsonSchemaGenerator;
import com.github.ibanetchep.msquests.core.util.JsonSchemaValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QuestActionFactory {
    private final Map<String, RegisteredAction<?>> registeredTypes = new HashMap<>();

    public record RegisteredAction<T extends QuestAction>(
            Class<T> actionClass,
            Function<QuestActionDTO, T> factory,
            String schema
    ) {
    }

    public <T extends QuestAction> void register(
            Class<T> actionClass,
            Function<QuestActionDTO, T> factory
    ) {
        ActionType annotation = actionClass.getAnnotation(ActionType.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    actionClass.getSimpleName() + " must have @ActionType annotation"
            );
        }

        String type = annotation.value();
        if (registeredTypes.containsKey(type)) {
            throw new IllegalStateException("Action type already registered: " + type);
        }

        registeredTypes.put(type, new RegisteredAction<>(actionClass, factory, JsonSchemaGenerator.generateSchema(actionClass)));
    }

    public QuestAction createAction(QuestActionDTO config) {
        RegisteredAction<?> registered = registeredTypes.get(config.type());

        if (registered == null) {
            throw new IllegalArgumentException("Unknown action type: " + config.type());
        }

        JsonSchemaValidator.validate(config.config(), registered.schema());

        return registered.factory().apply(config);
    }
}
