package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionMode;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class ActorQuestGroup {

    private final QuestActor actor;
    private final QuestGroupConfig groupConfig;
    private final Map<String, List<Quest>> questsByKey = new ConcurrentHashMap<>();

    public ActorQuestGroup(QuestActor actor, QuestGroupConfig groupConfig) {
        this.actor = actor;
        this.groupConfig = groupConfig;
    }

    /**
     * Add a quest instance for its key.
     */
    public void addQuest(Quest quest) {
        String key = quest.getQuestConfig().getKey();
        questsByKey.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(quest);
    }

    /**
     * Remove a quest instance (useful when cancelling / finishing + cleanup).
     */
    public void removeQuest(Quest quest) {
        String key = quest.getQuestConfig().getKey();
        List<Quest> list = questsByKey.get(key);
        if (list != null) {
            list.remove(quest);
            if (list.isEmpty()) {
                questsByKey.remove(key);
            }
        }
    }

    /** Helper: stream of all quest instances (flattened) */
    private Stream<Quest> allQuestsStream() {
        return questsByKey.values().stream().flatMap(List::stream);
    }

    /** Returns a (possibly empty) list of all quest instances for the given quest key. */
    public List<Quest> getAllQuestsForKey(String questKey) {
        return Collections.unmodifiableList(questsByKey.getOrDefault(questKey, List.of()));
    }

    public @Nullable Quest getActiveQuestByKey(String questKey) {
        return getAllQuestsForKey(questKey).stream().filter(Quest::isActive).findFirst().orElse(null);
    }

    /** Returns true if the actor ever started this quest key (any instance exists). */
    public boolean hasStarted(String questKey) {
        List<Quest> list = questsByKey.get(questKey);
        return list != null && !list.isEmpty();
    }

    public boolean hasActive(String questKey) {
        return getActiveQuestByKey(questKey) != null;
    }

    public int getCompletedCount() {
        return (int) allQuestsStream()
                .filter(q -> q.getStatus() == QuestStatus.COMPLETED)
                .count();
    }

    public int getInProgressCount() {
        return (int) allQuestsStream()
                .filter(Quest::isActive)
                .count();
    }

    public List<QuestConfig> getNotInProgress() {
        return groupConfig.getOrderedQuests().stream()
                .filter(qc -> !hasActive(qc.getKey()))
                .toList();
    }

    public List<QuestConfig> getNeverStarted() {
        return groupConfig.getOrderedQuests().stream()
                .filter(qc -> !hasStarted(qc.getKey()))
                .toList();
    }

    public int currentPeriodQuestCount() {
        Instant periodStart = groupConfig.getPeriodStart();
        Instant periodEnd = groupConfig.getPeriodEnd();

        return (int) allQuestsStream()
                .filter(q -> {
                    Instant createdAt = q.getCreatedAt().toInstant();
                    return (periodStart == null || createdAt.isAfter(periodStart))
                            && (periodEnd == null || createdAt.isBefore(periodEnd));
                })
                .count();
    }

    public List<QuestConfig> getAvailableToDistribute() {
        List<QuestConfig> candidates = new ArrayList<>();

        if (!groupConfig.isActive()) {
            return List.of();
        }

        // Do not distribute quests if they need to be manually selected.
        if(groupConfig.getDistributionMode() == QuestDistributionMode.MANUAL) {
            return List.of();
        }

        int maxActive = groupConfig.getMaxActive();
        Integer maxPerPeriod = groupConfig.getMaxPerPeriod();

        int inProgressCount = getInProgressCount();
        int maxToStart = maxActive;

        maxToStart = Math.min(maxToStart, maxActive - inProgressCount);


        if (maxPerPeriod != null) {
            int remainingInPeriod = Math.max(0, maxPerPeriod - currentPeriodQuestCount());
            maxToStart = Math.min(maxToStart, remainingInPeriod);
        }

        if (maxToStart <= 0) {
            return List.of();
        }

        switch (groupConfig.getDistributionMode()) {
            case SEQUENTIAL -> {
                List<QuestConfig> neverStarted = getNeverStarted();
                if (!neverStarted.isEmpty()) {
                    candidates = neverStarted.stream().limit(maxToStart).toList();
                } else if (groupConfig.isRepeatable()) {
                    candidates = getNotInProgress().stream().limit(maxToStart).toList();
                }
            }

            case RANDOM -> {
                if (groupConfig.isRepeatable()) {
                    candidates.addAll(getNotInProgress());
                } else {
                    candidates.addAll(getNeverStarted());
                }

                Collections.shuffle(candidates);
                candidates = candidates.stream().limit(maxToStart).toList();
            }
        }

        return candidates;
    }


    public List<QuestConfig> getSelectableQuests() {
        if (!groupConfig.isActive()) {
            return List.of();
        }

        if(groupConfig.getDistributionMode() != QuestDistributionMode.MANUAL) {
            return List.of();
        }

        List<QuestConfig> candidates = new ArrayList<>();

        if (groupConfig.isRepeatable()) {
            candidates.addAll(getNotInProgress());
        } else {
            candidates.addAll(getNeverStarted());
        }

        return candidates;
    }

    public boolean canStart(QuestConfig config) {
        if (!groupConfig.isActive()) return false;

        if (hasActive(config.getKey())) return false;
        if (!groupConfig.isRepeatable() && hasStarted(config.getKey())) return false;

        int maxActive = groupConfig.getMaxActive();
        Integer maxPerPeriod = groupConfig.getMaxPerPeriod();

        int inProgress = getInProgressCount();
        if (inProgress >= maxActive) return false;

        int periodCount = currentPeriodQuestCount();
        if (maxPerPeriod != null && periodCount >= maxPerPeriod) return false;

        return true;
    }

}
