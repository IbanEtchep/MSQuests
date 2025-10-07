package com.github.ibanetchep.msquests.core.quest.player;

import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerProfile {

    private final UUID id;
    private final Map<UUID, QuestActor> actors = new HashMap<>();
    private @Nullable UUID trackedQuestId;

    public PlayerProfile(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public @Nullable Quest getTrackedQuest() {
        return getQuests().get(trackedQuestId);
    }

    public @Nullable UUID getTrackedQuestId() {
        return trackedQuestId;
    }

    public void setTrackedQuestId(@Nullable UUID trackedQuestId) {
        this.trackedQuestId = trackedQuestId;
    }

    public void addActor(QuestActor actor) {
        actors.put(actor.getId(), actor);
        actor.addProfile(this);
    }

    public void removeActor(QuestActor actor) {
        actor.removeProfile(this);
        actors.remove(id);
    }

    public Map<UUID, QuestActor> getActors() {
        return actors;
    }

    public Map<UUID, Quest> getQuests() {
        return actors.values().stream()
                .flatMap(actor -> actor.getQuests().values().stream())
                .collect(Collectors.toMap(Quest::getId, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends QuestObjective> List<T> getActiveObjectivesByType(String objectiveType) {
        return getActors().values().stream()
                .flatMap(actor -> actor.getActiveObjectivesByType(objectiveType).stream())
                .map(o -> (T) o)
                .toList();
    }
}