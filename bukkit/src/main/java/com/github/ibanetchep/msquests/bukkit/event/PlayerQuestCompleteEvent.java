package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.core.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerQuestCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private Player player;
    private final Quest quest;

    public PlayerQuestCompleteEvent(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public Player getPlayer() {
        return player;
    }

    public Quest getQuest() {
        return quest;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
