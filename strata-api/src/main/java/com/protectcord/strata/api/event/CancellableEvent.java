package com.protectcord.strata.api.event;

/**
 * An event that can be cancelled by a subscriber to prevent default behavior.
 */
public abstract class CancellableEvent implements StrataEvent {

    private boolean cancelled;

    @Override
    public boolean isCancellable() {
        return true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
