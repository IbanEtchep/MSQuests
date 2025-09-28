package com.github.ibanetchep.msquests.core.mapper;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.dto.QuestObjectiveDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import java.util.Map;
import java.util.stream.Collectors;

public class QuestMapper {

    public QuestDTO toDTO(Quest quest) {
        Map<String, QuestObjectiveDTO> objectiveDtos = quest.getObjectives().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            QuestObjective objective = entry.getValue();
                            return new QuestObjectiveDTO(
                                    objective.getQuest().getId(),
                                    objective.getObjectiveConfig().getKey(),
                                    objective.getType(),
                                    objective.getStatus(),
                                    objective.progressToJson()
                            );
                        }
                ));

        QuestConfig questConfig = quest.getQuestConfig();

        return new QuestDTO(
                quest.getId(),
                questConfig.getKey(),
                questConfig.getGroup().getKey(),
                quest.getActor().getId(),
                quest.getStatus(),
                quest.getCompletedAt() != null ? quest.getCompletedAt().getTime() : null,
                quest.getCreatedAt().getTime(),
                quest.getUpdatedAt().getTime(),
                objectiveDtos
        );
    }
}
