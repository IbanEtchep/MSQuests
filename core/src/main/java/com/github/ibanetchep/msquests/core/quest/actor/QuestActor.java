package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor {

    protected UUID id;
    protected String name;
    protected final Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    protected final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

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
    public abstract boolean isMember(UUID playerId);

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

    public @Nullable Quest getActiveQuestByKey(String key) {
        return quests.values().stream()
                .filter(quest -> quest.getQuestConfig().getKey().equals(key))
                .filter(Quest::isActive)
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

    public List<Quest> getQuestsByGroup(QuestGroupConfig questGroupConfig, int page) {
        return quests.values().stream()
                .filter(quest -> quest.getQuestGroup() == questGroupConfig)
                .skip((page - 1) * 10L)
                .limit(10)
                .toList();
    }

    public int getQuestsByGroupCount(QuestGroupConfig questGroupConfig) {
        return (int) quests.values().stream()
                .filter(quest -> quest.getQuestGroup() == questGroupConfig)
                .count();
    }

    public Collection<PlayerProfile> getProfiles() {
        return profiles.values();
    }

    public void addProfile(PlayerProfile playerProfile) {
        profiles.put(playerProfile.getId(), playerProfile);
    }

    public void removeProfile(PlayerProfile playerProfile) {
        profiles.remove(playerProfile.getId());
    }
}
