package com.github.ibanetchep.msquests.model;

import java.util.Map;
import java.util.UUID;

public class Category {

    private UUID uniqueId;
    private String name;
    private String description;
    private Map<UUID, QuestEntry> quests;

}
