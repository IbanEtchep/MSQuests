package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor implements PlaceholderProvider {

    protected UUID id;
    protected String name;
    protected final Map<String, ActorQuestGroup> groups = new ConcurrentHashMap<>();
    protected final Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    protected final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

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

        QuestGroupConfig groupConfig = quest.getQuestGroup();
        groups.computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig)).addQuest(quest);
    }

    public @Nullable ActorQuestGroup getActorQuestGroup(QuestGroupConfig groupConfig) {
        if(!groupConfig.getActorType().equalsIgnoreCase(getActorType())) {
            return null;
        }

        return groups.computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig));
    }

    public void removeQuest(Quest quest) {
        quests.remove(quest.getId());
    }

    @SuppressWarnings("unchecked")
    public <T extends QuestObjective> List<T> getActiveObjectivesByType(String objectiveType) {
        return quests.values().stream()
                .filter(Quest::isActive)
                .flatMap(quest -> quest.getActiveObjectives().stream())
                .filter(objective -> objective.getType().equals(objectiveType))
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

    @Override
    public Map<String, String> getPlaceholders(Translator translator) {
        return Map.of(
                "actor_id", id.toString(),
                "actor_name", name
        );
    }
}
