package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor {

    protected UUID id;
    protected String name;
    protected final Map<UUID, Quest> quests = new ConcurrentHashMap<>();

    protected final Map<String, Set<QuestObjective<?>>> objectivesByType = new ConcurrentHashMap<>();

    public QuestActor(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getActorType();
    public abstract boolean isActor(UUID playerId);

    public Map<UUID, Quest> getQuests() {
        return Collections.unmodifiableMap(quests);
    }

    public void addQuest(Quest quest) {
        quests.put(quest.getId(), quest);
        quest.setActor(this);

        quest.getObjectives().values().forEach(this::addObjective);
    }

    public void removeQuest(Quest quest) {
        quests.remove(quest.getId());

        quest.getObjectives().values().forEach(this::removeObjective);
    }

    public void addObjective(QuestObjective<?> objective) {
        objectivesByType.computeIfAbsent(objective.getType(), k -> ConcurrentHashMap.newKeySet())
                .add(objective);
    }

    public void removeObjective(QuestObjective<?> objective) {
        Set<QuestObjective<?>> set = objectivesByType.get(objective.getType());
        if (set != null) {
            set.remove(objective);
            if (set.isEmpty()) {
                objectivesByType.remove(objective.getType());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends QuestObjective<?>> List<T> getObjectivesByType(String objectiveType) {
        return objectivesByType.getOrDefault(objectiveType, Set.of()).stream()
                .map(o -> (T) o)
                .toList();
    }
}
