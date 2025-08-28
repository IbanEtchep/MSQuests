package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.bukkit.MSQuestsPlugin;
import com.github.ibanetchep.msquests.core.event.CancellableCoreEvent;
import com.github.ibanetchep.msquests.core.event.CoreEvent;
import com.github.ibanetchep.msquests.core.event.CoreQuestStartEvent;
import com.github.ibanetchep.msquests.core.event.EventDispatcher;
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
    }

    @Override
    public void dispatch(CoreEvent event) {
        Consumer<CoreEvent> handler = handlers.get(event.getClass());
        if (handler != null) {
            handler.accept(event);
        }
    }

    private void callQuestStart(CoreQuestStartEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            QuestActor actor = event.getActor();

            if(!actor.isActor(player.getUniqueId())) {
                continue;
            }

            PlayerQuestStartEvent bukkitEvent = new PlayerQuestStartEvent(player, actor, event.getQuestConfig());
            plugin.getServer().getPluginManager().callEvent(bukkitEvent);
            if (event instanceof CancellableCoreEvent cancellable) {
                cancellable.setCancelled(bukkitEvent.isCancelled());
            }
        }
    }

}
