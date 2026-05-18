package com.protectcord.strata.nms;

/**
 * Updates native heightmaps from Strata's computed heightmap data.
 */
public interface HeightmapWriter {

    /**
     * Writes heightmap data to a native chunk.
     */
    void writeHeightmaps(Object nativeChunk, int[] worldSurface, int[] motionBlocking);
}
