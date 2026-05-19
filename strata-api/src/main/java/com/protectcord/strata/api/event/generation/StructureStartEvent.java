package com.protectcord.strata.api.event.generation;

import com.protectcord.strata.api.event.StrataEvent;

/**
 * Fired when a structure begins placement during chunk generation.
 *
 * <p>This is a read-only notification event; it cannot be cancelled.</p>
 *
 * @since 1.0.0
 * @see com.protectcord.strata.api.event.EventBus
 */
public class StructureStartEvent implements StrataEvent {

    private final int chunkX;
    private final int chunkZ;
    private final String worldName;
    private final String structureKey;

    /**
     * Constructs a new structure start event.
     *
     * @param chunkX       the chunk X coordinate
     * @param chunkZ       the chunk Z coordinate
     * @param worldName    the name of the world the chunk belongs to
     * @param structureKey the key identifying the structure being placed
     */
    public StructureStartEvent(int chunkX, int chunkZ, String worldName, String structureKey) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldName = worldName;
        this.structureKey = structureKey;
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

    /**
     * Returns the key identifying the structure being placed.
     *
     * @return the structure key, never {@code null}
     */
    public String structureKey() { return structureKey; }
}
