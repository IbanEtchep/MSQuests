package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.core.quest.config.QuestConfig;
import com.github.ibanetchep.msquests.core.quest.actor.QuestActor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a quest starts, for each player that is part of the quest.
 */
public class PlayerQuestStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final QuestActor actor;
    private final QuestConfig questConfig;

    public PlayerQuestStartEvent(Player player, QuestActor actor, QuestConfig questConfig) {
        this.player = player;
        this.actor = actor;
        this.questConfig = questConfig;
    }

    public Player getPlayer() {
        return player;
    }

    public QuestActor getActor() {
        return actor;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
