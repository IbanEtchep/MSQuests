package com.github.ibanetchep.msquests.core.platform;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.cache.PlayerProfileCache;
import com.github.ibanetchep.msquests.core.cache.QuestActorCache;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.core.service.QuestService;

import java.util.logging.Logger;

public interface MSQuestsPlatform {

    Logger getLogger();
    EventDispatcher getEventDispatcher();
    QuestService getQuestService();
    QuestLifecycleService getQuestLifecycleService();
    QuestActorCache getQuestActorRegistry();
    QuestConfigRegistry getQuestConfigRegistry();
    PlayerProfileCache getPlayerProfileRegistry();
    ActorTypeRegistry getActorTypeRegistry();
    QuestObjectiveFactory getObjectiveTypeRegistry();

}
