package com.github.ibanetchep.msquests.core.platform;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.registry.*;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.core.service.QuestService;

import java.util.logging.Logger;

public interface MSQuestsPlatform {

    Logger getLogger();
    EventDispatcher getEventDispatcher();
    QuestService getQuestService();
    QuestLifecycleService getQuestLifecycleService();
    QuestActorRegistry getQuestActorRegistry();
    QuestConfigRegistry getQuestRegistry();
    PlayerProfileRegistry getPlayerProfileRegistry();
    ActorTypeRegistry getActorTypeRegistry();
    ObjectiveTypeRegistry getObjectiveTypeRegistry();

}
