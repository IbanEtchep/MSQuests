package com.github.ibanetchep.msquests.core.quest;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class QuestProxy extends Quest {

    boolean dirty = false;

    public QuestProxy(UUID uniqueId, QuestConfig quest, QuestActor actor, QuestStatus status, @Nullable Date completedAt, Date createdAt, Date updatedAt) {
        super(uniqueId, quest, actor, status, completedAt, createdAt, updatedAt);
    }

    @Override
    public void setStatus(QuestStatus status) {
        super.setStatus(status);
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }
}
