package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BukkitEventDispatcher implements EventDispatcher {

    private final MSQuestsPlugin plugin;
    private final Map<Class<? extends CoreEvent>, Consumer<CoreEvent>> handlers = new HashMap<>();

    public BukkitEventDispatcher(MSQuestsPlugin plugin) {
        this.plugin = plugin;

        handlers.put(CoreQuestStartEvent.class, e -> callQuestStart((CoreQuestStartEvent) e));
        handlers.put(CoreQuestStartedEvent.class, e -> callQuestStarted((CoreQuestStartedEvent) e));
        handlers.put(CoreQuestCompletedEvent.class, e -> callQuestComplete((CoreQuestCompletedEvent) e));
        handlers.put(CoreQuestObjectiveProgressEvent.class, e -> callQuestObjectiveProgress((CoreQuestObjectiveProgressEvent) e));
        handlers.put(CoreQuestObjectiveCompletedEvent.class, e -> callQuestObjectiveComplete((CoreQuestObjectiveCompletedEvent) e));
        handlers.put(CoreQuestObjectiveProgressedEvent.class, e -> callQuestObjectiveProgressed((CoreQuestObjectiveProgressedEvent) e));
    }

    @Override
    public void dispatch(CoreEvent event) {
        Consumer<CoreEvent> handler = handlers.get(event.getClass());
        if (handler != null) {
            handler.accept(event);
        }
    }

    private void callQuestStart(CoreQuestStartEvent event) {
        QuestStartEvent startEvent = new QuestStartEvent(event.getActor(), event.getQuestConfig());
        plugin.getServer().getPluginManager().callEvent(startEvent);
        event.setCancelled(startEvent.isCancelled());
    }

    private void callQuestStarted(CoreQuestStartedEvent event) {
        QuestStartedEvent startedEvent = new QuestStartedEvent(event.getQuest());
        plugin.getServer().getPluginManager().callEvent(startedEvent);
    }

    private void callQuestComplete(CoreQuestCompletedEvent event) {
        QuestCompleteEvent completeEvent = new QuestCompleteEvent(event.getQuest());
        plugin.getServer().getPluginManager().callEvent(completeEvent);
    }

    private void callQuestObjectiveProgress(CoreQuestObjectiveProgressEvent event) {
        QuestObjective objective = event.getObjective();
        PlayerProfile profile = event.getPlayerProfile();

        ObjectiveProgressEvent progressEvent = new ObjectiveProgressEvent(objective, profile);
        plugin.getServer().getPluginManager().callEvent(progressEvent);
        event.setCancelled(progressEvent.isCancelled());
    }

    private void callQuestObjectiveComplete(CoreQuestObjectiveCompletedEvent event) {
        QuestObjective objective = event.getObjective();
        PlayerProfile profile = event.getPlayerProfile();

        ObjectiveCompletedEvent completeEvent = new ObjectiveCompletedEvent(objective, profile);
        plugin.getServer().getPluginManager().callEvent(completeEvent);
    }

    private void callQuestObjectiveProgressed(CoreQuestObjectiveProgressedEvent event) {
        QuestObjective objective = event.getObjective();
        PlayerProfile profile = event.getPlayerProfile();
        ObjectiveProgressedEvent progressedEvent = new ObjectiveProgressedEvent(objective, profile);
        plugin.getServer().getPluginManager().callEvent(progressedEvent);
    }

}
