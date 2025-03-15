package com.github.ibanetchep.msquests.model;

import com.github.ibanetchep.msquests.model.actor.QuestActor;
import com.github.ibanetchep.msquests.objective.QuestObjectiveEntry;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class QuestEntry {

    private UUID uniqueId;
    private Quest quest;
    private QuestStatus status;
    private QuestActor actor;
    private Map<UUID, QuestObjectiveEntry> objectives;
    private Date startedAt;
    private Date expiresAt;
    private Date createdAt;
    private Date updatedAt;

    public UUID getUniqueId() {
        return uniqueId;
    }

    public QuestActor getActor() {
        return actor;
    }

}
