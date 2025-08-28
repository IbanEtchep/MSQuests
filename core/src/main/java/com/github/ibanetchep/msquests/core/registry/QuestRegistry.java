package com.github.ibanetchep.msquests.core.registry;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.group.QuestGroup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestRegistry {

    private final Map<String, QuestGroup> questGroups = new ConcurrentHashMap<>();
    private final Map<UUID, QuestActor> actors = new ConcurrentHashMap<>();

    public List<QuestActor> getActors(UUID playerId) {
        return actors.values().stream()
                .filter(actor -> actor.isActor(playerId))
                .toList();
    }

    public Map<UUID, QuestActor> getActors() {
        return Collections.unmodifiableMap(actors);
    }

    public Map<String, QuestGroup> getQuestGroups() {
        return Collections.unmodifiableMap(questGroups);
    }

    public List<Quest> getActiveQuests(UUID playerId) {
        return getActors(playerId).stream()
                .flatMap(actor -> actor.getQuests().values().stream())
                .filter(Quest::isActive)
                .toList();
    }

    public Quest getLastQuest(QuestActor actor, QuestConfig questConfig) {
        return actor.getQuests().values().stream()
                .filter(q -> q.getQuestConfig().getKey().equals(questConfig.getKey()))
                .max(Comparator.comparing(Quest::getCreatedAt))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends QuestObjective<?>> List<T> getObjectivesByType(UUID playerId, String objectiveType) {
        return getActors(playerId).stream()
                .flatMap(actor -> actor.getObjectivesByType(objectiveType).stream())
                .map(o -> (T) o)
                .toList();
    }

    public Collection<QuestActor> getActorsByType(String actorType) {
        return actors.values().stream()
                .filter(actor -> actor.getActorType().equals(actorType))
                .toList();
    }

    public void clearQuestGroups() {
        questGroups.clear();
    }

    public void registerQuestGroup(QuestGroup questGroup) {
        questGroups.put(questGroup.getKey(), questGroup);
    }

    public void removeActor(UUID id) {
        actors.remove(id);
    }

    public void registerActor(QuestActor actor) {
        actors.put(actor.getId(), actor);
    }
}
