package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConditionConfigDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStage;
import com.github.ibanetchep.msquests.core.quest.condition.QuestObjectiveCondition;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ObjectiveType;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjectiveStatus;
import com.github.ibanetchep.msquests.core.util.JsonSchemaGenerator;
import com.github.ibanetchep.msquests.core.util.JsonSchemaValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QuestObjectiveFactory {

    private record Type<C extends QuestObjectiveConfig, O extends QuestObjective>(
            Class<C> configClass,
            Class<O> objectiveClass,
            String schema
    ) {
        C createConfig(QuestObjectiveConfigDTO dto) {
            try {
                return configClass.getConstructor(QuestObjectiveConfigDTO.class).newInstance(dto);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate params: " +
                        configClass.getSimpleName(), e);
            }
        }

        O createObjective(QuestStage stage, C config, int progress, QuestObjectiveStatus status) {
            try {
                return objectiveClass
                        .getConstructor(QuestStage.class, configClass, int.class, QuestObjectiveStatus.class)
                        .newInstance(stage, config, progress, status);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate objective: " + objectiveClass.getSimpleName(), e);
            }
        }
    }

    private final Map<String, Type<?, ?>> types = new HashMap<>();
    private final QuestObjectiveConditionFactory conditionFactory;

    public QuestObjectiveFactory(QuestObjectiveConditionFactory conditionFactory) {
        this.conditionFactory = conditionFactory;
    }

    public <C extends QuestObjectiveConfig, O extends QuestObjective> void register(
            Class<C> configClass,
            Class<O> objectiveClass
    ) {
        ObjectiveType annotation = configClass.getAnnotation(ObjectiveType.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    configClass.getSimpleName() + " must have @ObjectiveType annotation"
            );
        }

        String type = annotation.value();
        if (types.containsKey(type)) {
            throw new IllegalStateException("Objective type already registered: " + type);
        }

        types.put(type, new Type<>(configClass, objectiveClass, JsonSchemaGenerator.generateSchema(configClass)));
    }

    public QuestObjectiveConfig createConfig(QuestObjectiveConfigDTO dto) {
        Type<?, ?> type = types.get(dto.type());
        if (type == null) throw new IllegalArgumentException("Unknown objective type: " + dto.type());
        JsonSchemaValidator.validate(dto.params(), type.schema());
        QuestObjectiveConfig config = type.createConfig(dto);
        config.setConditions(resolveConditions(dto.params()));
        return config;
    }

    @SuppressWarnings("unchecked")
    public QuestObjective createObjective(QuestStage stage, QuestObjectiveConfig config, int progress, QuestObjectiveStatus status) {
        Type<QuestObjectiveConfig, QuestObjective> type = (Type<QuestObjectiveConfig, QuestObjective>) types.get(config.getType());
        if (type == null) throw new IllegalArgumentException("Unknown objective type: " + config.getType());
        return type.createObjective(stage, config, progress, status);
    }

    @SuppressWarnings("unchecked")
    private List<QuestObjectiveCondition> resolveConditions(Map<String, Object> params) {
        Object raw = params.get("conditions");
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream()
                .filter(item -> item instanceof Map)
                .map(item -> {
                    Map<String, Object> map = (Map<String, Object>) item;
                    return conditionFactory.build(new QuestObjectiveConditionConfigDTO((String) map.get("type"), map));
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
