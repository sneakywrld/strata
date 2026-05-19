package com.protectcord.strata.api.event.lifecycle;

import com.protectcord.strata.api.event.StrataEvent;

/**
 * Fired when a Strata-managed world is unloaded.
 *
 * <p>This is a read-only notification event; it cannot be cancelled. Plugins can use it
 * to clean up world-specific resources or persist state.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.event.EventBus
 */
public class WorldUnloadEvent implements StrataEvent {

    private final String worldName;

    /**
     * Constructs a new world unload event.
     *
     * @param worldName the name of the world being unloaded
     */
    public WorldUnloadEvent(String worldName) {
        this.worldName = worldName;
    }

    /**
     * Returns the name of the world being unloaded.
     *
     * @return the world name, never {@code null}
     */
    public String worldName() { return worldName; }
}
