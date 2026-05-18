package com.protectcord.strata.api.carver;

/**
 * A bitmask tracking which block positions in a chunk have been carved by {@link Carver}s.
 *
 * <p>The carving mask serves two purposes:</p>
 * <ul>
 *   <li>Prevents multiple carvers from conflicting by allowing later carvers to check
 *       whether a position has already been carved.</li>
 *   <li>Allows surface rules and feature decorators to detect carved regions and adjust
 *       their behavior (e.g., skipping grass placement above open caves).</li>
 * </ul>
 *
 * <p>Coordinates are chunk-local (0-15 for X and Z, minY to maxY for Y).</p>
 *
 * @since 1.0.0
 * @see Carver#carve(CarverContext, CarvingMask)
 */
public interface CarvingMask {

    /**
     * Marks a position as carved.
     *
     * @param x the chunk-local X coordinate (0-15)
     * @param y the world Y coordinate
     * @param z the chunk-local Z coordinate (0-15)
     */
    void set(int x, int y, int z);

    /**
     * Returns {@code true} if the specified position has been carved.
     *
     * @param x the chunk-local X coordinate (0-15)
     * @param y the world Y coordinate
     * @param z the chunk-local Z coordinate (0-15)
     * @return {@code true} if the position was previously marked with {@link #set(int, int, int)}
     */
    boolean get(int x, int y, int z);

    /**
     * Returns the total number of block positions that have been carved in this chunk.
     *
     * @return the carved block count
     */
    int carvedCount();
}
