package com.protectcord.strata.nms;

import com.protectcord.strata.api.block.StrataBlockState;

/**
 * Maps between Strata block states and native NMS block states efficiently.
 */
public interface BlockStateMapper {

    /**
     * Sets a block in a native chunk section.
     */
    void setBlock(Object chunkSection, int x, int y, int z, StrataBlockState state);

    /**
     * Gets a block from a native chunk section.
     */
    StrataBlockState getBlock(Object chunkSection, int x, int y, int z);
}
