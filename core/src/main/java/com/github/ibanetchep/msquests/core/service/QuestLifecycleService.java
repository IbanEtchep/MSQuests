package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionConfig;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionTrigger;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestDistributionStrategy;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.platform.MSQuestsPlatform;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import com.github.ibanetchep.msquests.core.quest.result.QuestRotateResult;
import com.github.ibanetchep.msquests.core.quest.result.QuestStartResult;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.repository.RotationRepository;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestLifecycleService {

    private static final Logger logger = Logger.getLogger(QuestLifecycleService.class.getName());

    private final EventDispatcher dispatcher;
    private final AtomicQuestExecutor executor;
    private final QuestService persistenceService;
    private final QuestFactory questFactory;
    private final QuestRegistry questRegistry;
    private final QuestConfigRegistry questConfigRegistry;
    private final QuestDistributionService distributionManager;
    private final RotationRepository rotationRepository;
    private final MSQuestsPlatform platform;

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            QuestConfigRegistry questConfigRegistry,
            AtomicQuestExecutor executor,
            QuestDistributionService distributionManager,
            RotationRepository rotationRepository,
            MSQuestsPlatform platform
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.questConfigRegistry = questConfigRegistry;
        this.executor = executor;
        this.distributionManager = distributionManager;
        this.rotationRepository = rotationRepository;
        this.platform = platform;
    }

    /**
     * Starts a quest for the given actor.
     * @param actor the actor to start the quest for
     * @param questConfig the quest configuration to use
     * @param strategy the distribution strategy to use
     * @return the result of the quest start attempt
     */
    public QuestStartResult startQuest(QuestActor actor, QuestConfig questConfig, QuestDistributionStrategy strategy, @Nullable PlayerProfile profile) {
        QuestStartResult validationResult = distributionManager.canStartQuest(actor, questConfig, strategy, profile);
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

        quest.getQuestGroup().getQuestStartActions().forEach(a -> a.execute(quest));

        questRegistry.add(quest);
        persistenceService.saveQuest(quest);

        return QuestStartResult.SUCCESS;
    }

    public CompletableFuture<Quest> completeObjective(QuestObjective objective, @Nullable PlayerProfile profile) {
        UUID questId = objective.getQuest().getId();

        return executor.execute(questId, quest -> {
            objective.complete();
            var objectiveCompletedEvent = new CoreQuestObjectiveCompletedEvent(objective, profile);
            dispatcher.dispatch(objectiveCompletedEvent);

            QuestGroupConfig groupConfig = quest.getQuestGroup();
            Map<String, String> context = Map.of(
                    "quest_name", quest.getQuestConfig().getName(),
                    "quest_key", quest.getQuestConfig().getKey()
            );
            groupConfig.getObjectiveCompleteActions().stream()
                    .filter(a -> profile == null || a.testConditions(profile, context))
                    .forEach(a -> a.execute(objective));

            if (quest.shouldComplete()) {
                finalizeQuestCompletion(quest);
            }

            persistenceService.saveQuest(objective.getQuest()).join();
        });
    }

    public void completeQuest(Quest quest) {
        executor.execute(quest.getId(), updatedQuest -> {
            finalizeQuestCompletion(updatedQuest);
            persistenceService.saveQuest(updatedQuest).join();
        });
    }

    private void finalizeQuestCompletion(Quest quest) {
        var questCompleteEvent = new CoreQuestCompletedEvent(quest);
        dispatcher.dispatch(questCompleteEvent);
        quest.setStatus(QuestStatus.COMPLETED);

        QuestGroupConfig groupConfig = quest.getQuestGroup();

        if (groupConfig.hasDistributionTrigger(DistributionTrigger.QUEST_COMPLETE)) {
            triggerDistribution(quest.getActor(), groupConfig);
        }

        platform.runSync(() -> {
            for (QuestAction reward : quest.getQuestConfig().getRewards()) {
                reward.execute(quest);
            }
            groupConfig.getQuestCompleteActions().forEach(a -> a.execute(quest));
            checkAllPeriodQuestsComplete(quest.getActor(), groupConfig);
        });
    }

    /**
     * Expires all quests that should expire for the given actor.
     * Sets status synchronously in memory, persists asynchronously.
     * @param actor the actor to expire quests for
     * @return true if any quests were expired
     */
    public boolean expireQuests(QuestActor actor) {
        List<Quest> toExpire = actor.getQuests().values().stream()
                .filter(Quest::shouldExpire)
                .toList();

        toExpire.forEach(quest -> {
            quest.setStatus(QuestStatus.EXPIRED);
            persistenceService.saveQuest(quest);
        });

        return !toExpire.isEmpty();
    }

    /**
     * Rotates a quest for the given actor, replacing it with a random alternative from the same group.
     * The old quest is deleted and a new one is started.
     * @param actor the actor to rotate the quest for
     * @param quest the quest to rotate
     * @return the result of the rotation attempt
     */
    public QuestRotateResult rotateQuest(QuestActor actor, Quest quest) {
        QuestGroupConfig groupConfig = quest.getQuestGroup();

        if (!groupConfig.isRotatable()) {
            return QuestRotateResult.NOT_ROTATABLE;
        }

        if (!quest.isActive()) {
            return QuestRotateResult.QUEST_NOT_ACTIVE;
        }

        ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
        if (actorGroup == null) {
            return QuestRotateResult.GROUP_NOT_FOUND;
        }

        if (!actorGroup.canRotate()) {
            return QuestRotateResult.MAX_ROTATIONS_REACHED;
        }

        String oldQuestKey = quest.getQuestConfig().getKey();

        List<QuestConfig> candidates = new ArrayList<>(actorGroup.getNotInProgress());
        candidates.removeIf(qc -> qc.getKey().equals(oldQuestKey));
        Collections.shuffle(candidates);

        if (candidates.isEmpty()) {
            return QuestRotateResult.NO_ALTERNATIVE_AVAILABLE;
        }

        // Delete old quest
        actorGroup.removeQuest(quest);
        actor.removeQuest(quest);
        questRegistry.remove(quest);
        persistenceService.deleteQuest(quest).join();

        // Start new quest
        for (QuestConfig candidate : candidates) {
            QuestStartResult startResult = startQuest(actor, candidate, QuestDistributionStrategy.RANDOM, null);
            if (startResult.isSuccess()) {
                actorGroup.incrementRotations();
                rotationRepository.save(actor.getId(), groupConfig.getKey()).exceptionally(e -> {
                    logger.log(Level.WARNING, "Failed to save rotation count", e);
                    return null;
                });
                return QuestRotateResult.SUCCESS;
            }
        }

        return QuestRotateResult.NO_ALTERNATIVE_AVAILABLE;
    }

    /**
     * Expires outdated quests and fires actor_load actions for all matching groups.
     * @param actor the actor to refresh
     */
    public void refreshActor(QuestActor actor) {
        boolean expired = expireQuests(actor);
        if (expired) {
            fireActorLoadActions(actor);
        }
    }

    /**
     * Fires actor_load actions for all matching groups.
     * @param actor the actor to fire actions for
     */
    public void fireActorLoadActions(QuestActor actor) {
        for (QuestGroupConfig groupConfig : questConfigRegistry.getQuestGroupConfigs().values()) {
            if (!groupConfig.getActorType().equalsIgnoreCase(actor.getActorType())) continue;
            if (!groupConfig.isActive()) continue;

            groupConfig.getActorLoadActions().forEach(action -> action.execute(actor, groupConfig));

            if (groupConfig.hasDistributionTrigger(DistributionTrigger.ACTOR_LOAD)) {
                triggerDistribution(actor, groupConfig);
            }
        }
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
            logger.warning("distributeQuests: actorQuestGroup is null for actor " + actor.getName() + " (type=" + actor.getActorType() + ") group " + groupConfig.getKey() + " (type=" + groupConfig.getActorType() + ")");
            return 0;
        }

        int startedCount = 0;
        List<QuestConfig> candidates = distributionManager.getCandidatesForStrategy(groupConfig, strategy);

        for(QuestConfig candidate : candidates) {
            if(startedCount >= maxToDistribute) {
                break;
            }

            QuestStartResult result = startQuest(actor, candidate, strategy, null);

            if (result.isFailure()) {
                continue;
            }

            startedCount++;
        }

        return startedCount;
    }

    private void checkAllPeriodQuestsComplete(QuestActor actor, QuestGroupConfig groupConfig) {
        List<QuestAction> actions = groupConfig.getAllPeriodQuestsCompleteActions();
        if (actions.isEmpty()) {
            return;
        }

        ActorQuestGroup actorGroup = actor.getActorQuestGroup(groupConfig);
        if (actorGroup == null) {
            return;
        }

        // All quests complete = none still active (in progress) AND at least one completed
        boolean allCompleted = actorGroup.getInProgressCount() == 0
                && actorGroup.getCompletedCount() > 0;

        if (allCompleted) {
            actions.forEach(a -> a.execute(actor, groupConfig));
        }
    }

    /**
     * Triggers distribution for an actor based on the group's distribution config.
     * Also fires quest_distribution actions if any quests were distributed.
     * @param actor the actor to distribute quests to
     * @param groupConfig the group configuration with distribution settings
     */
    public void triggerDistribution(QuestActor actor, QuestGroupConfig groupConfig) {
        DistributionConfig dc = groupConfig.getDistributionConfig();
        if (dc == null) return;

        int distributed = distributeQuests(actor, groupConfig, dc.getStrategy(), dc.getAmount());
        if (distributed > 0) {
            groupConfig.getQuestDistributionActions().forEach(a -> a.execute(actor, groupConfig));
        }
    }
}
