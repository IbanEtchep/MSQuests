package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a quest starts.
 */
public class QuestStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestActor actor;
    private final QuestConfig questConfig;
    private boolean cancelled;

    public QuestStartEvent(QuestActor actor, QuestConfig questConfig) {
        this.actor = actor;
        this.questConfig = questConfig;
    }

    public QuestActor getActor() {
        return actor;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
