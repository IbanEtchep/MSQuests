package com.github.ibanetchep.msquests.core.event;

public abstract class CoreEvent {
    private final long timestamp;

    public CoreEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}