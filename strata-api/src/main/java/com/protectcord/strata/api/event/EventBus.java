package com.protectcord.strata.api.event;

import java.util.function.Consumer;

/**
 * Strata's event bus for subscribing to generation lifecycle events.
 * Events are fired during chunk generation and plugin lifecycle.
 *
 * <p>Example usage:
 * <pre>
 * api.eventBus().subscribe(ChunkGeneratedEvent.class, event -> {
 *     // React to chunk generation
 * });
 * </pre>
 */
public interface EventBus {

    /**
     * Subscribes to an event type.
     *
     * @param eventType the event class
     * @param handler   the handler to invoke
     * @param <T>       the event type
     */
    <T extends StrataEvent> void subscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Subscribes with a priority (lower = called first).
     */
    <T extends StrataEvent> void subscribe(Class<T> eventType, int priority, Consumer<T> handler);

    /**
     * Unsubscribes a handler.
     */
    <T extends StrataEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Fires an event to all subscribers.
     *
     * @return the event (may have been modified by handlers)
     */
    <T extends StrataEvent> T fire(T event);
}
