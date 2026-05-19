package com.protectcord.strata.api.event.bus;

/**
 * Priority levels for event handler execution order.
 *
 * <p>Handlers with lower priority are invoked first. {@link #MONITOR} should only be used
 * for read-only observation and must not modify or cancel events.</p>
 *
 * @since 1.0.0
 * @see Subscribe
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}
