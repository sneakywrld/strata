package com.protectcord.strata.api.event;

import java.util.function.Consumer;

/**
 * Strata's event bus for subscribing to generation and plugin lifecycle events.
 *
 * <p>Events are fired during chunk generation, profile loading, and world creation.
 * Subscribers are invoked synchronously in priority order (lower priority values first).
 * The event bus is accessed via {@link com.protectcord.strata.api.core.StrataAPI#eventBus()}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * EventBus bus = api.eventBus();
 * bus.subscribe(ChunkGeneratedEvent.class, event -> {
 *     logger.info("Chunk " + event.chunk() + " generated in " + event.generationTimeMs() + "ms");
 * });
 *
 * // Subscribe with priority (lower = called first)
 * bus.subscribe(ChunkGeneratingEvent.class, 10, event -> {
 *     if (shouldSkipStage(event.stage())) {
 *         event.setCancelled(true);
 *     }
 * });
 * }</pre>
 *
 * <p>Available event types:</p>
 * <ul>
 *   <li>{@link ChunkGeneratingEvent} -- fired before each pipeline stage (cancellable)</li>
 *   <li>{@link ChunkGeneratedEvent} -- fired after all stages complete</li>
 *   <li>{@link ProfileLoadedEvent} -- fired when a profile is loaded or reloaded</li>
 *   <li>{@link WorldCreatedEvent} -- fired when a new Strata world is created</li>
 * </ul>
 *
 * @since 1.0.0
 * @see StrataEvent
 * @see CancellableEvent
 */
public interface EventBus {

    /**
     * Subscribes to an event type with default priority (0).
     *
     * @param eventType the event class to subscribe to
     * @param handler   the handler to invoke when the event fires
     * @param <T>       the event type
     */
    <T extends StrataEvent> void subscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Subscribes to an event type with an explicit priority.
     *
     * <p>Handlers with lower priority values are invoked first. Handlers at the same
     * priority have undefined invocation order.</p>
     *
     * @param eventType the event class to subscribe to
     * @param priority  the handler priority (lower = invoked earlier)
     * @param handler   the handler to invoke when the event fires
     * @param <T>       the event type
     */
    <T extends StrataEvent> void subscribe(Class<T> eventType, int priority, Consumer<T> handler);

    /**
     * Unsubscribes a previously registered handler from an event type.
     *
     * <p>The handler reference must be the same instance that was passed to
     * {@link #subscribe(Class, Consumer)}. If the handler is not found, this method is a no-op.</p>
     *
     * @param eventType the event class to unsubscribe from
     * @param handler   the handler to remove
     * @param <T>       the event type
     */
    <T extends StrataEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * Fires an event to all subscribers synchronously in priority order.
     *
     * <p>For {@link CancellableEvent} instances, handlers may cancel the event to prevent
     * default behavior. The returned event object reflects any modifications made by handlers.</p>
     *
     * @param event the event to fire
     * @param <T>   the event type
     * @return the same event instance, potentially modified by handlers (e.g., cancelled)
     */
    <T extends StrataEvent> T fire(T event);
}
