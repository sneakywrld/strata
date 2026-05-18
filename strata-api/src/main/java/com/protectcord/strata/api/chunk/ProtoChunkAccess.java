package com.protectcord.strata.api.chunk;

/**
 * Combined access interface for a chunk under construction during generation.
 * This is the primary handle passed to pipeline stages, providing access to
 * blocks, biomes, heightmaps, and chunk metadata.
 */
public interface ProtoChunkAccess extends BlockAccess, BiomeAccess {

    /**
     * Returns the chunk coordinate.
     */
    ChunkCoord coord();

    /**
     * Returns the highest non-air block at the given column.
     */
    int getHeight(int x, int z);

    /**
     * Returns the highest non-air block at the given column for a specific heightmap type.
     */
    int getHeight(HeightmapType type, int x, int z);

    /**
     * Marks a position as part of the lighting update queue.
     */
    void markForLightUpdate(int x, int y, int z);
}
