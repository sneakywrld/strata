package com.protectcord.strata.api.event;

/**
 * An event that can be cancelled by a subscriber to prevent default behavior.
 *
 * <p>When a handler calls {@link #setCancelled(boolean) setCancelled(true)}, subsequent handlers
 * still receive the event (and can un-cancel it), but the system action associated with the
 * event is skipped if the event remains cancelled after all handlers have run.</p>
 *
 * <p>For example, cancelling a {@link ChunkGeneratingEvent} causes the associated pipeline
 * stage to be skipped for that chunk.</p>
 *
 * @since 1.0.0
 * @see StrataEvent
 * @see EventBus
 */
public abstract class CancellableEvent implements StrataEvent {

    private boolean cancelled;

    @Override
    public boolean isCancellable() {
        return true;
    }

    /**
     * Returns {@code true} if this event has been cancelled.
     *
     * @return {@code true} if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event.
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to un-cancel it
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
