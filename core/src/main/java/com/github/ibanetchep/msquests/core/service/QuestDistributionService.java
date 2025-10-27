package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestDistributionService {

    /**
     * Check if a specific quest can be started, returning detailed result.
     */
    public QuestStartResult canStartQuest(QuestActor actor, QuestConfig questConfig, QuestDistributionStrategy strategy) {
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

        return QuestStartResult.SUCCESS;
    }

    public List<QuestConfig> getCandidatesForStrategy(
            QuestGroupConfig groupConfig,
            QuestDistributionStrategy strategy
    ) {
        List<QuestConfig> candidates = new ArrayList<>(groupConfig.getOrderedQuests());

        if (strategy == QuestDistributionStrategy.RANDOM) {
            Collections.shuffle(candidates);
        }

        return candidates;
    }
}