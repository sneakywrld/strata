package com.protectcord.strata.api.event;

/**
 * Base interface for all Strata events dispatched through the {@link EventBus}.
 *
 * <p>Events that support cancellation should extend {@link CancellableEvent} rather than
 * implementing this interface directly. Read-only notification events (e.g.,
 * {@link ChunkGeneratedEvent}, {@link ProfileLoadedEvent}) implement this interface directly.</p>
 *
 * @since 1.0.0
 * @see EventBus
 * @see CancellableEvent
 */
public interface StrataEvent {

    /**
     * Returns {@code true} if this event can be cancelled by handlers.
     *
     * <p>Events extending {@link CancellableEvent} return {@code true}; all others return
     * {@code false} by default.</p>
     *
     * @return {@code true} if this event supports cancellation
     */
    default boolean isCancellable() {
        return false;
    }
}
