package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.dto.QuestDTO;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestStage;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestStageConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;

import java.util.Date;
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

        for (QuestStageConfig stageConfig : config.getStages().values()) {
            QuestStage stage = new QuestStage(quest, stageConfig);

            for (QuestObjectiveConfig objConfig : stageConfig.getObjectives().values()) {
                QuestObjective objective = questObjectiveFactory.createObjective(stage, objConfig, 0);
                stage.addObjective(objective);
            }

            quest.addStage(stage);
        }

        actor.addQuest(quest);
        return quest;
    }

    public Quest createQuest(QuestConfig config, QuestActor actor) {
        return this.createQuest(
                UUID.randomUUID(),
                actor,
                config,
                QuestStatus.IN_PROGRESS,
                null,
                new Date(),
                new Date()
        );
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

        for (QuestStageConfig stageConfig : config.getStages().values()) {
            QuestStage stage = new QuestStage(quest, stageConfig);

            for (QuestObjectiveConfig objConfig : stageConfig.getObjectives().values()) {
                int progress = 0;

                if (dto.objectives() != null && dto.objectives().containsKey(objConfig.getKey())) {
                    progress = dto.objectives().get(objConfig.getKey()).progress();
                }

                QuestObjective objective = questObjectiveFactory.createObjective(stage, objConfig, progress);
                stage.addObjective(objective);
            }

            quest.addStage(stage);
        }

        actor.addQuest(quest);
        return quest;
    }
}