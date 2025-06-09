package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.core.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when quest handling is triggered by a player.
 * For example, when a player the /quest player <player> handle <quest> command is executed
 * when clicking an NPC to handle a quest.
 */
public class PlayerQuestHandleEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Quest quest;
    private final Player player;

    public PlayerQuestHandleEvent(Quest quest, Player player) {
        this.quest = quest;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Quest getQuest() {
        return quest;
    }

    public Player getPlayer() {
        return player;
    }
}
