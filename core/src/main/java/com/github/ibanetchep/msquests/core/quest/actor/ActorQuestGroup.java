package com.github.ibanetchep.msquests.core.quest.actor;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ActorQuestGroup {

    private final QuestActor actor;
    private final QuestGroupConfig groupConfig;
    private final Map<String, List<Quest>> questsByKey = new ConcurrentHashMap<>();
    private final AtomicInteger rotationsInPeriod = new AtomicInteger(0);

    public ActorQuestGroup(QuestActor actor, QuestGroupConfig groupConfig) {
        this.actor = actor;
        this.groupConfig = groupConfig;
    }

    public QuestGroupConfig getGroupConfig() {
        return groupConfig;
    }

    public QuestActor getActor() {
        return actor;
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

    /** Returns true if the actor has a quest with this key created within the current period (any status except EXPIRED). */
    public boolean hasStartedInCurrentPeriod(String questKey) {
        List<Quest> list = questsByKey.get(questKey);
        if (list == null || list.isEmpty()) return false;
        return list.stream().anyMatch(this::isLiveAttempt);
    }

    /**
     * The actor's attempt at this quest right now: the running instance, or failing that the
     * most recent one created in the current period. Null when the quest has never been
     * started, or when the only instances are EXPIRED — an expired attempt is over, and the
     * quest becomes offerable again.
     *
     * <p>Every display surface asks this question (quest menus, placeholders, the Artisan
     * catalog rows). It is answered once, here, so they cannot drift apart.
     */
    public @Nullable Quest getCurrentAttempt(String questKey) {
        Quest active = getActiveQuestByKey(questKey);
        if (active != null) return active;
        return getAllQuestsForKey(questKey).stream()
                .filter(this::isLiveAttempt)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    /** Not expired, and created inside the group's current period. */
    private boolean isLiveAttempt(Quest quest) {
        if (quest.getStatus() == QuestStatus.EXPIRED) return false;
        Instant periodStart = groupConfig.getPeriodStart();
        Instant periodEnd = groupConfig.getPeriodEnd();
        Instant createdAt = quest.getCreatedAt().toInstant();
        return (periodStart == null || !createdAt.isBefore(periodStart))
                && (periodEnd == null || createdAt.isBefore(periodEnd));
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
                .filter(q -> q.getStatus() != QuestStatus.EXPIRED)
                .filter(q -> {
                    Instant createdAt = q.getCreatedAt().toInstant();
                    return (periodStart == null || createdAt.isAfter(periodStart))
                            && (periodEnd == null || createdAt.isBefore(periodEnd));
                })
                .count();
    }

    public int getRotationsInPeriod() {
        return rotationsInPeriod.get();
    }

    public void incrementRotations() {
        rotationsInPeriod.incrementAndGet();
    }

    public void resetRotations() {
        rotationsInPeriod.set(0);
    }

    public boolean canRotate() {
        if (!groupConfig.isRotatable()) {
            return false;
        }
        Integer max = groupConfig.getMaxRotationsPerPeriod();
        return max == null || rotationsInPeriod.get() < max;
    }

}
