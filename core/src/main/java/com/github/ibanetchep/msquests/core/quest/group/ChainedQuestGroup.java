package com.github.ibanetchep.msquests.core.quest.group;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;

import java.time.Instant;

public class ChainedQuestGroup extends QuestGroup {

    public ChainedQuestGroup(String key, String name, String description, Instant startAt, Instant endAt) {
        super(key, name, description, startAt, endAt);
    }

    @Override
    public QuestGroupType getType() {
        return QuestGroupType.CHAINED;
    }

    @Override
    public QuestConfig getNextQuest(QuestConfig current) {
        return null;
    }
}
