package com.github.ibanetchep.msquests.core.platform;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.factory.QuestObjectiveFactory;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.PlayerProfileRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestActorRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestConfigRegistry;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.core.service.QuestService;

import java.util.logging.Logger;

public interface MSQuestsPlatform {

    Logger getLogger();
    EventDispatcher getEventDispatcher();
    QuestService getQuestService();
    QuestLifecycleService getQuestLifecycleService();
    QuestActorRegistry getQuestActorRegistry();
    QuestConfigRegistry getQuestConfigRegistry();
    PlayerProfileRegistry getPlayerProfileRegistry();
    ActorTypeRegistry getActorTypeRegistry();
    QuestObjectiveFactory getObjectiveTypeRegistry();

}
