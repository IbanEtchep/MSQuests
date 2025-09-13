package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    public abstract void sendMessage(String message);

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

    public @Nullable Quest getQuestByKey(String key) {
        return quests.values().stream()
                .filter(quest -> quest.getQuestConfig().getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    private void addObjective(QuestObjective<?> objective) {
        objectivesByType.computeIfAbsent(objective.getType(), k -> ConcurrentHashMap.newKeySet())
                .add(objective);
    }

    private void removeObjective(QuestObjective<?> objective) {
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

    public Map<QuestGroup, List<Quest>> getQuestsByGroup() {
        return quests.values().stream().collect(Collectors.groupingBy(Quest::getQuestGroup));
    }
}
