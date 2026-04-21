package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class QuestDistributionService {

    /**
     * Check if a specific quest can be started, returning detailed result.
     */
    public QuestStartResult canStartQuest(QuestActor actor, QuestConfig questConfig, QuestDistributionStrategy strategy, @Nullable PlayerProfile profile) {
        QuestGroupConfig groupConfig = questConfig.getGroupConfig();
        ActorQuestGroup group = actor.getActorQuestGroup(groupConfig);

        if(!groupConfig.getActorType().equalsIgnoreCase(actor.getActorType())) {
            return QuestStartResult.INVALID_ACTOR_TYPE;
        }

        if (group == null) {
            return QuestStartResult.GROUP_NOT_FOUND;
        }

        if (!groupConfig.isActive()) {
            return QuestStartResult.GROUP_INACTIVE;
        }

        if (group.hasActive(questConfig.getKey())) {
            return QuestStartResult.ALREADY_ACTIVE;
        }

        if (strategy == QuestDistributionStrategy.SEQUENTIAL && group.hasStarted(questConfig.getKey())) {
            return QuestStartResult.ALREADY_COMPLETED;
        }

        if (groupConfig.getMaxPerPeriod() != null && group.hasStartedInCurrentPeriod(questConfig.getKey())) {
            return QuestStartResult.ALREADY_COMPLETED;
        }

        int maxActive = groupConfig.getMaxActive();
        int inProgress = group.getInProgressCount();
        if (inProgress >= maxActive) {
            return QuestStartResult.MAX_ACTIVE_REACHED;
        }

        Integer maxPerPeriod = groupConfig.getMaxPerPeriod();
        if (maxPerPeriod != null) {
            int periodCount = group.currentPeriodQuestCount();
            if (periodCount >= maxPerPeriod) {
                return QuestStartResult.PERIOD_LIMIT_REACHED;
            }
        }

        if (profile != null && !questConfig.getConditions().stream().allMatch(c -> c.test(profile))) {
            return QuestStartResult.CONDITIONS_NOT_MET;
        }

        return QuestStartResult.SUCCESS;
    }

    public List<QuestConfig> getCandidatesForStrategy(
            QuestGroupConfig groupConfig,
            QuestDistributionStrategy strategy
    ) {
        List<QuestConfig> candidates = new ArrayList<>(groupConfig.getOrderedQuests());

        if (strategy == QuestDistributionStrategy.RANDOM) {
            if (groupConfig.hasTierDistribution()) {
                return getCandidatesWithTierDistribution(candidates, groupConfig.getTierDistribution());
            }
            Collections.shuffle(candidates);
        }

        return candidates;
    }

    private List<QuestConfig> getCandidatesWithTierDistribution(
            List<QuestConfig> allQuests,
            Map<String, Integer> tierDistribution
    ) {
        Map<String, List<QuestConfig>> byTier = allQuests.stream()
                .filter(q -> q.getTier() != null)
                .collect(Collectors.groupingBy(QuestConfig::getTier));

        List<QuestConfig> result = new ArrayList<>();
        Set<String> selectedKeys = new HashSet<>();

        for (Map.Entry<String, Integer> entry : tierDistribution.entrySet()) {
            String tier = entry.getKey();
            int count = entry.getValue();

            List<QuestConfig> pool = new ArrayList<>(byTier.getOrDefault(tier, List.of()));
            Collections.shuffle(pool);

            for (QuestConfig quest : pool) {
                if (result.size() >= getTotalRequested(tierDistribution)) break;
                if (selectedKeys.add(quest.getKey())) {
                    result.add(quest);
                    if (--count <= 0) break;
                }
            }
        }

        int totalRequested = getTotalRequested(tierDistribution);
        if (result.size() < totalRequested) {
            List<QuestConfig> remaining = new ArrayList<>(allQuests);
            remaining.removeIf(q -> selectedKeys.contains(q.getKey()));
            Collections.shuffle(remaining);

            for (QuestConfig quest : remaining) {
                if (result.size() >= totalRequested) break;
                result.add(quest);
            }
        }

        Collections.shuffle(result);
        return result;
    }

    private int getTotalRequested(Map<String, Integer> tierDistribution) {
        return tierDistribution.values().stream().mapToInt(Integer::intValue).sum();
    }
}