package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestObjectiveConfigDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class QuestObjectiveFactory {

    private static class Type<C extends QuestObjectiveConfig, O extends QuestObjective> {
        private final Function<QuestObjectiveConfigDTO, C> configFactory;
        private final BiFunction<Quest, C, O> objectiveFactory;

        Type(Function<QuestObjectiveConfigDTO, C> configFactory,
             BiFunction<Quest, C, O> objectiveFactory) {
            this.configFactory = configFactory;
            this.objectiveFactory = objectiveFactory;
        }

        C createConfig(QuestObjectiveConfigDTO dto) {
            return configFactory.apply(dto);
        }

        O createObjective(Quest quest, C config) {
            return objectiveFactory.apply(quest, config);
        }
    }

    private final Map<String, Type<?, ?>> types = new HashMap<>();

    public <C extends QuestObjectiveConfig, O extends QuestObjective> void registerType(
            String key,
            Function<QuestObjectiveConfigDTO, C> configFactory,
            BiFunction<Quest, C, O> objectiveFactory
    ) {
        types.put(key, new Type<>(configFactory, objectiveFactory));
    }

    public QuestObjectiveConfig createConfig(QuestObjectiveConfigDTO dto) {
        Type<?, ?> type = types.get(dto.type());
        if (type == null) throw new IllegalArgumentException("Unknown objective type: " + dto.type());
        return type.createConfig(dto);
    }

    @SuppressWarnings("unchecked")
    public QuestObjective createObjective(Quest quest, QuestObjectiveConfig config) {
        Type<QuestObjectiveConfig, QuestObjective> type = (Type<QuestObjectiveConfig, QuestObjective>) types.get(config.getType());
        if (type == null) throw new IllegalArgumentException("Unknown objective type: " + config.getType());
        return type.createObjective(quest, config);
    }
}

