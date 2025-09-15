package com.github.ibanetchep.msquests.core.factory;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.QuestObjectiveConfig;
import com.github.ibanetchep.msquests.core.quest.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;

import java.util.Date;
import java.util.UUID;

public class QuestFactory {

    private final ObjectiveTypeRegistry objectiveTypeRegistry;

    public QuestFactory(ObjectiveTypeRegistry objectiveTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
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
            objectiveTypeRegistry.createObjective(quest, objectiveConfig, 0);
        }

        return quest;
    }

    public Quest createQuest(QuestConfig config, QuestActor actor) {
        return this.createQuest(UUID.randomUUID(), actor, config, QuestStatus.IN_PROGRESS, null, new Date(), new Date());
    }

}
