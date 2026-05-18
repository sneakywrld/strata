package com.protectcord.strata.api.chunk;

import com.protectcord.strata.api.block.StrataBlockState;

/**
 * Read-write access to block states within a chunk or region.
 * Used by pipeline stages to build terrain, place features, etc.
 */
public interface BlockAccess {

    /**
     * Gets the block state at the given coordinates.
     */
    StrataBlockState getBlock(int x, int y, int z);

    /**
     * Sets the block state at the given coordinates.
     */
    void setBlock(int x, int y, int z, StrataBlockState state);

    /**
     * Returns the minimum Y level for this access.
     */
    int minY();

    /**
     * Returns the maximum Y level (exclusive) for this access.
     */
    int maxY();
}
