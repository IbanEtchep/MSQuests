package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.factory.QuestFactory;
import com.github.ibanetchep.msquests.core.quest.actor.ActorQuestGroup;
import com.github.ibanetchep.msquests.core.quest.actor.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.actor.QuestStatus;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.config.action.QuestAction;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.quest.executor.AtomicQuestExecutor;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
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

    public QuestLifecycleService(
            EventDispatcher dispatcher,
            QuestService persistenceService,
            QuestFactory questFactory,
            QuestRegistry questRegistry,
            QuestConfigRegistry questConfigRegistry,
            AtomicQuestExecutor executor
    ) {
        this.dispatcher = dispatcher;
        this.persistenceService = persistenceService;
        this.questFactory = questFactory;
        this.questRegistry = questRegistry;
        this.questConfigRegistry = questConfigRegistry;
        this.executor = executor;
    }

    public boolean startQuest(QuestActor actor, QuestConfig questConfig) {
        Quest currentQuest = actor.getActiveQuestByKey(questConfig.getKey());

        if(currentQuest != null) {
            return false;
        }

        CoreQuestStartEvent event = new CoreQuestStartEvent(actor, questConfig);
        dispatcher.dispatch(event);

        if(event.isCancelled()) {
            return false;
        }

        Quest quest = questFactory.createQuest(questConfig, actor);

        CoreQuestStartedEvent startedEvent = new CoreQuestStartedEvent(quest);
        dispatcher.dispatch(startedEvent);

        questRegistry.add(quest);
        persistenceService.saveQuest(quest).join();

        return true;
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
     * Distribute quests from group where actor is eligible
     */
    public void distributeQuests(QuestActor actor) {
        for(QuestGroupConfig questGroupConfig : questConfigRegistry.getQuestGroupConfigs().values()) {
            ActorQuestGroup actorQuestGroup = actor.getActorQuestGroup(questGroupConfig);

            if(actorQuestGroup == null) {
                continue;
            }

            List<QuestConfig> toDistribute = actorQuestGroup.getAvailableToDistribute();
            toDistribute.forEach(questConfig -> {
                startQuest(actor, questConfig);
            });
        }
    }
}
