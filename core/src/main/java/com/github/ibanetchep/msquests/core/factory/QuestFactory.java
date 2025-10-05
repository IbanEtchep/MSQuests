package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class QuestFactory {

    private final QuestObjectiveFactory questObjectiveFactory;

    public QuestFactory(QuestObjectiveFactory questObjectiveFactory) {
        this.questObjectiveFactory = questObjectiveFactory;
    }

    public Quest createQuest(
            UUID id,
            QuestActor actor,
            QuestConfig config,
            QuestStatus status,
            Date completedAt,
            Date createdAt,
            Date updatedAt
    ) {
        Quest quest = new Quest(id, config, actor, status, completedAt, createdAt, updatedAt);

        for (QuestObjectiveConfig objectiveConfig : config.getObjectives().values()) {
            quest.addObjective(questObjectiveFactory.createObjective(quest, objectiveConfig, 0));
        }

        actor.addQuest(quest);

        return quest;
    }

    public Quest createQuest(QuestConfig config, QuestActor actor) {
        return this.createQuest(UUID.randomUUID(), actor, config, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
    }

    public Quest createQuest(QuestConfig config, QuestActor actor, QuestDTO dto) {
        Quest quest = new Quest(
                dto.id(),
                config,
                actor,
                dto.status(),
                dto.completedAt() != null ? new Date(dto.completedAt()) : null,
                dto.createdAt() != null ? new Date(dto.createdAt()) : null,
                dto.updatedAt() != null ? new Date(dto.updatedAt()) : null
        );

        if (dto.objectives() != null) {
            for (Map.Entry<String, QuestObjectiveConfig> entry : config.getObjectives().entrySet()) {
                String key = entry.getKey();
                QuestObjectiveConfig objectiveConfig = entry.getValue();

                int progress = 0;
                if (dto.objectives().containsKey(key)) {
                    progress = dto.objectives().get(key).progress();
                }

                quest.addObjective(questObjectiveFactory.createObjective(quest, objectiveConfig, progress));
            }
        } else {
            for (QuestObjectiveConfig objectiveConfig : config.getObjectives().values()) {
                quest.addObjective(questObjectiveFactory.createObjective(quest, objectiveConfig, 0));
            }
        }

        actor.addQuest(quest);
        return quest;
    }
}
