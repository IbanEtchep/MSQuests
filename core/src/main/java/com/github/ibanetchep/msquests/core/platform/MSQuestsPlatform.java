package com.github.ibanetchep.msquests.core.platform;

import com.github.ibanetchep.msquests.core.event.EventDispatcher;
import com.github.ibanetchep.msquests.core.registry.ActorTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.ObjectiveTypeRegistry;
import com.github.ibanetchep.msquests.core.registry.QuestRegistry;
import com.github.ibanetchep.msquests.core.service.QuestLifecycleService;
import com.github.ibanetchep.msquests.core.service.QuestPersistenceService;

import java.util.logging.Logger;

public interface MSQuestsPlatform {

    Logger getLogger();
    EventDispatcher getEventDispatcher();
    QuestPersistenceService getQuestPersistenceService();
    QuestLifecycleService getQuestLifecycleService();
    QuestRegistry getQuestRegistry();
    ActorTypeRegistry getActorRegistry();
    ObjectiveTypeRegistry getObjectiveTypeRegistry();

}
