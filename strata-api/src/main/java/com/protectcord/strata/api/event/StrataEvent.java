package com.protectcord.strata.api.event;

/**
 * Base interface for all Strata events.
 */
public interface StrataEvent {

    /**
     * Returns true if this event can be cancelled by handlers.
     */
    default boolean isCancellable() {
        return false;
    }
}
