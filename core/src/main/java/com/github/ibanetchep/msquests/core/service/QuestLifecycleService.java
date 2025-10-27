package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QuestLifecycleService {

    private final EventDispatcher dispatcher;
    private final AtomicQuestExecutor executor;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;
    private final QuestConfigRegistry questConfigRegistry;
    private final QuestDistributionService distributionManager;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            QuestConfigRegistry questConfigRegistry,
            AtomicQuestExecutor executor,
            QuestDistributionService distributionManager
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.questConfigRegistry = questConfigRegistry;
        this.executor = executor;
        this.distributionManager = distributionManager;
    }

    /**
     * Starts a quest for the given actor.
     * @param actor the actor to start the quest for
     * @param questConfig the quest configuration to use
     * @param strategy the distribution strategy to use
     * @return the result of the quest start attempt
     */
    public QuestStartResult startQuest(QuestActor actor, QuestConfig questConfig, QuestDistributionStrategy strategy) {
        QuestStartResult validationResult = distributionManager.canStartQuest(actor, questConfig, strategy);
        if(validationResult.isFailure()) {
            return validationResult;
        }

        CoreQuestStartEvent event = new CoreQuestStartEvent(actor, questConfig);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return QuestStartResult.CANCELLED_BY_EVENT;
        }

        Quest quest = questFactory.createQuest(questConfig, actor);

        CoreQuestStartedEvent startedEvent = new CoreQuestStartedEvent(quest);
        dispatcher.dispatch(startedEvent);

        questRegistry.add(quest);
        persistenceService.saveQuest(quest).join();

        return QuestStartResult.SUCCESS;
    }

    public CompletableFuture<Quest> completeObjective(QuestObjective objective, @Nullable PlayerProfile profile) {
        UUID questId = objective.getQuest().getId();

        return executor.execute(questId, quest -> {
            objective.complete();
            var objectiveCompletedEvent = new CoreQuestObjectiveCompletedEvent(objective, profile);
            dispatcher.dispatch(objectiveCompletedEvent);

            if (quest.shouldComplete()) {
                var questCompleteEvent = new CoreQuestCompletedEvent(quest);
                dispatcher.dispatch(questCompleteEvent);
                quest.setStatus(QuestStatus.COMPLETED);
            }

            persistenceService.saveQuest(objective.getQuest()).join();
        });
    }

    public void completeQuest(Quest quest) {
        executor.execute(quest.getId(), updatedQuest -> {
            updatedQuest.setStatus(QuestStatus.COMPLETED);

            CoreQuestCompletedEvent event = new CoreQuestCompletedEvent(updatedQuest);
            dispatcher.dispatch(event);

            for (QuestAction questAction : updatedQuest.getQuestConfig().getRewards()) {
                questAction.execute(updatedQuest);
            }

            persistenceService.saveQuest(updatedQuest).join();
        });
    }

    /**
     * Expires all quests that should expire for the given actor.
     * @param actor the actor to expire quests for
     */
    public void expireQuests(QuestActor actor) {
        actor.getQuests().values().stream()
                .filter(Quest::shouldExpire)
                .forEach(this::expireQuestIfNeeded);
    }

    /**
     * Expires the given quest.
     * @param quest the quest to expire
     */
    public void expireQuestIfNeeded(Quest quest) {
        executor.execute(quest.getId(), updatedQuest -> {
            if (updatedQuest.shouldExpire()) {
                updatedQuest.setStatus(QuestStatus.EXPIRED);
                persistenceService.saveQuest(updatedQuest);
            }
        });
    }

    /**
     * Distributes quests to the given actor based on the specified strategy.
     * @param actor the actor to distribute quests to
     * @param groupConfig the group configuration to use
     * @param strategy the distribution strategy to use
     * @param maxToDistribute the maximum number of quests to distribute
     * @return the number of quests distributed
     */
    public int distributeQuests(QuestActor actor, QuestGroupConfig groupConfig, QuestDistributionStrategy strategy, int maxToDistribute) {
        ActorQuestGroup actorQuestGroup = actor.getActorQuestGroup(groupConfig);

        if(actorQuestGroup == null) {
            return 0;
        }

        int startedCount = 0;
        List<QuestConfig> candidates = distributionManager.getCandidatesForStrategy(groupConfig, strategy);

        for(QuestConfig candidate : candidates) {
            if(startedCount >= maxToDistribute) {
                break;
            }

            QuestStartResult result = startQuest(actor, candidate, strategy);

            if (result.isFailure()) {
                continue;
            }

            startedCount++;
        }

        return startedCount;
    }
}
