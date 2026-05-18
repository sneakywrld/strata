package com.protectcord.strata.api.chunk;

/**
 * Immutable chunk coordinate pair.
 */
public record ChunkCoord(int x, int z) {

    /**
     * Returns the world-space block X of this chunk's origin corner.
     */
    public int blockX() {
        return x << 4;
    }

    /**
     * Returns the world-space block Z of this chunk's origin corner.
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
