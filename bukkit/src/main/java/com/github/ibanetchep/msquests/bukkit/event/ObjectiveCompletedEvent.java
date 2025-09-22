package com.github.ibanetchep.msquests.bukkit.event;

import com.github.ibanetchep.msquests.core.quest.objective.QuestObjective;
import com.github.ibanetchep.msquests.core.quest.player.PlayerProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event triggered when a quest starts.
 */
public class ObjectiveCompletedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestObjective objective;
    private final @Nullable PlayerProfile profile;

    /**
     * @param objective - progressed objective
     * @param profile - profile who triggered the progress
     */
    public ObjectiveCompletedEvent(QuestObjective objective, @Nullable PlayerProfile profile) {
        this.objective = objective;
        this.profile = profile;
    }

    public QuestObjective getObjective() {
        return  objective;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @Nullable PlayerProfile getPlayerProfile() {
        return profile;
    }
}
