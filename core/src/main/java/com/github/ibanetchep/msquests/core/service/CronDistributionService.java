package com.github.ibanetchep.msquests.core.service;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.group.DistributionTrigger;
import com.github.ibanetchep.msquests.core.quest.config.group.QuestGroupConfig;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.util.CronUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CronDistributionService {

    private static final Logger logger = Logger.getLogger(CronDistributionService.class.getName());

    private final QuestConfigRegistry questConfigRegistry;
    private final QuestActorRegistry questActorRegistry;
    private final QuestLifecycleService questLifecycleService;
    private final Map<String, Instant> lastResetTimes = new ConcurrentHashMap<>();

    public CronDistributionService(
            QuestConfigRegistry questConfigRegistry,
            QuestActorRegistry questActorRegistry,
            QuestLifecycleService questLifecycleService
    ) {
        this.questConfigRegistry = questConfigRegistry;
        this.questActorRegistry = questActorRegistry;
        this.questLifecycleService = questLifecycleService;
    }

    public void tick() {
        Instant now = Instant.now();

        for (QuestGroupConfig groupConfig : questConfigRegistry.getQuestGroupConfigs().values()) {
            if (!groupConfig.isActive()) continue;
            if (groupConfig.getResetCron() == null) continue;
            if (!groupConfig.hasDistributionTrigger(DistributionTrigger.PERIOD_RESET)) continue;

            Instant previousReset = CronUtils.getPreviousExecution(groupConfig.getResetCron(), now);
            if (previousReset == null) continue;

            Instant lastKnown = lastResetTimes.get(groupConfig.getKey());
            if (lastKnown != null && !previousReset.isAfter(lastKnown)) continue;

            // Record the new baseline
            lastResetTimes.put(groupConfig.getKey(), previousReset);

            // Skip distribution on the first tick (baseline initialization)
            if (lastKnown == null) continue;

            logger.info("Period reset detected for group " + groupConfig.getKey() + ", distributing quests to online actors.");

            for (QuestActor actor : questActorRegistry.getActors().values()) {
                if (!groupConfig.getActorType().equalsIgnoreCase(actor.getActorType())) continue;
                if (actor.getProfiles().isEmpty()) continue;
                questLifecycleService.expireQuests(actor);
                questLifecycleService.triggerDistribution(actor, groupConfig);
            }
        }
    }

}
