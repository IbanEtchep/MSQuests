package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.event.*;
import com.github.ibanetchep.msquests.core.quest.Quest;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BukkitEventDispatcher implements EventDispatcher {

    private final MSQuestsPlugin plugin;
    private final Map<Class<? extends CoreEvent>, Consumer<CoreEvent>> handlers = new HashMap<>();

    public BukkitEventDispatcher(MSQuestsPlugin plugin) {
        this.plugin = plugin;

        handlers.put(CoreQuestStartEvent.class, e -> callQuestStart((CoreQuestStartEvent) e));
        handlers.put(CoreQuestCompleteEvent.class, e -> callQuestComplete((CoreQuestCompleteEvent) e));
    }

    @Override
    public void dispatch(CoreEvent event) {
        Consumer<CoreEvent> handler = handlers.get(event.getClass());
        if (handler != null) {
            handler.accept(event);
        }
    }

    private void callQuestStart(CoreQuestStartEvent event) {
        Quest quest = event.getQuest();
        QuestActor actor = quest.getActor();

        QuestStartEvent startEvent = new QuestStartEvent(event.getQuest());
        plugin.getServer().getPluginManager().callEvent(startEvent);
        event.setCancelled(startEvent.isCancelled());

        if(event.isCancelled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if(!actor.isMember(player.getUniqueId())) {
                continue;
            }

            PlayerQuestStartEvent playerEvent = new PlayerQuestStartEvent(player, quest);
            plugin.getServer().getPluginManager().callEvent(playerEvent);
        }
    }

    private void callQuestComplete(CoreQuestCompleteEvent event) {
        Quest quest = event.getQuest();
        QuestActor actor = quest.getActor();

        QuestCompleteEvent completeEvent = new QuestCompleteEvent(quest);
        plugin.getServer().getPluginManager().callEvent(completeEvent);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(!actor.isMember(player.getUniqueId())) {
                continue;
            }

            PlayerQuestCompleteEvent bukkitEvent = new PlayerQuestCompleteEvent(player, quest);
            plugin.getServer().getPluginManager().callEvent(bukkitEvent);
        }
    }

}
