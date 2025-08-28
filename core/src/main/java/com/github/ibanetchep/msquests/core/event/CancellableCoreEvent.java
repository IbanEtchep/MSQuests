package com.github.ibanetchep.msquests.core.event;

public abstract class CancellableCoreEvent extends CoreEvent {

    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}