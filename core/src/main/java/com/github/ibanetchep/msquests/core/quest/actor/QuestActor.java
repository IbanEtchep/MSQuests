package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.lang.PlaceholderProvider;
import com.github.ibanetchep.msquests.core.lang.Translator;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class QuestActor implements PlaceholderProvider {

    protected UUID id;
    protected String name;
    /*
     * Both maps are swapped as a whole by replaceLoadedQuests, so a reload never exposes an
     * empty or half-filled actor, and never stacks the reloaded quests on top of the old ones.
     */
    protected volatile Map<String, ActorQuestGroup> groups = new ConcurrentHashMap<>();
    protected volatile Map<UUID, Quest> quests = new ConcurrentHashMap<>();
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

    public synchronized void addQuest(Quest quest) {
        quests.put(quest.getId(), quest);
        quest.setActor(this);

        QuestGroupConfig groupConfig = quest.getQuestGroup();
        groups.computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig)).addQuest(quest);
    }

    /**
     * Replaces every loaded quest with the ones just read from storage, in one step.
     *
     * <p>Quests started after {@code loadStartedAt} are kept even if the read missed them: their
     * insert may not have reached the database yet when the read ran.
     *
     * @param rotationsByGroup rotations already used this period, per group
     * @return the quests the actor held before, for callers that must drop them elsewhere
     */
    public synchronized Collection<Quest> replaceLoadedQuests(
            Collection<Quest> loaded,
            Map<QuestGroupConfig, Integer> rotationsByGroup,
            Instant loadStartedAt
    ) {
        Map<UUID, Quest> previous = quests;
        Map<UUID, Quest> nextQuests = new ConcurrentHashMap<>();
        Map<String, ActorQuestGroup> nextGroups = new ConcurrentHashMap<>();

        List<Quest> toAdd = new ArrayList<>(loaded);
        for (Quest quest : previous.values()) {
            boolean missedByRead = loaded.stream().noneMatch(q -> q.getId().equals(quest.getId()))
                    && quest.getCreatedAt() != null
                    && !quest.getCreatedAt().toInstant().isBefore(loadStartedAt);
            if (missedByRead) {
                toAdd.add(quest);
            }
        }

        for (Quest quest : toAdd) {
            quest.setActor(this);
            nextQuests.put(quest.getId(), quest);
            QuestGroupConfig groupConfig = quest.getQuestGroup();
            nextGroups.computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig)).addQuest(quest);
        }

        rotationsByGroup.forEach((groupConfig, count) -> nextGroups
                .computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig))
                .setRotations(count));

        quests = nextQuests;
        groups = nextGroups;
        return previous.values();
    }

    public @Nullable ActorQuestGroup getActorQuestGroup(QuestGroupConfig groupConfig) {
        if(!groupConfig.getActorType().equalsIgnoreCase(getActorType())) {
            return null;
        }

        return groups.computeIfAbsent(groupConfig.getKey(), k -> new ActorQuestGroup(this, groupConfig));
    }

    public synchronized void removeQuest(Quest quest) {
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
