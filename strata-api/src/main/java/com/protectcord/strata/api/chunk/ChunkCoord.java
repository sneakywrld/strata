package com.protectcord.strata.api.chunk;

/**
 * Immutable chunk coordinate pair representing a 16x16 block column in the world.
 *
 * <p>Chunk coordinates are obtained by right-shifting block coordinates by 4 (dividing by 16).
 * This record provides utilities for converting between block and chunk coordinates,
 * as well as packing/unpacking to a {@code long} key for efficient hash map storage.</p>
 *
 * @param x the chunk X coordinate (block X >> 4)
 * @param z the chunk Z coordinate (block Z >> 4)
 * @since 1.0.0
 */
public record ChunkCoord(int x, int z) {

    /**
     * Returns the world-space block X coordinate of this chunk's origin (minimum) corner.
     *
     * @return the block X coordinate ({@code x << 4})
     */
    public int blockX() {
        return x << 4;
    }

    /**
     * Returns the world-space block Z coordinate of this chunk's origin (minimum) corner.
     *
     * @return the block Z coordinate ({@code z << 4})
     */
    public int blockZ() {
        return z << 4;
    }

    /**
     * Returns the chunk coordinate containing the given block coordinate.
     */
    public static ChunkCoord fromBlock(int blockX, int blockZ) {
        return new ChunkCoord(blockX >> 4, blockZ >> 4);
    }

    /**
     * Returns a unique long key for this chunk, suitable for use in hash maps.
     */
    public long toLong() {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Reconstructs a ChunkCoord from a packed long key.
     */
    public static ChunkCoord fromLong(long packed) {
        return new ChunkCoord((int) (packed >> 32), (int) packed);
    }
}
