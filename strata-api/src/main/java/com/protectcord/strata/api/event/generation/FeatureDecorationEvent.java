package com.protectcord.strata.api.event.generation;

import com.protectcord.strata.api.event.StrataEvent;

/**
 * Fired during the feature decoration phase of chunk generation (trees, ores, flowers, etc.).
 *
 * <p>This is a read-only notification event; it cannot be cancelled.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.event.EventBus
 */
public class FeatureDecorationEvent implements StrataEvent {

    private final int chunkX;
    private final int chunkZ;
    private final String worldName;

    /**
     * Constructs a new feature decoration event.
     *
     * @param chunkX    the chunk X coordinate
     * @param chunkZ    the chunk Z coordinate
     * @param worldName the name of the world the chunk belongs to
     */
    public FeatureDecorationEvent(int chunkX, int chunkZ, String worldName) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldName = worldName;
    }

    /**
     * Returns the chunk X coordinate.
     *
     * @return the chunk X coordinate
     */
    public int chunkX() { return chunkX; }

    /**
     * Returns the chunk Z coordinate.
     *
     * @return the chunk Z coordinate
     */
    public int chunkZ() { return chunkZ; }

    /**
     * Returns the name of the world this chunk belongs to.
     *
     * @return the world name, never {@code null}
     */
    public String worldName() { return worldName; }
}
