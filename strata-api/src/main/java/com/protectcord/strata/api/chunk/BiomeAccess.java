package com.protectcord.strata.api.chunk;

import com.protectcord.strata.api.biome.Biome;

/**
 * Read-write access to biome data within a chunk.
 * Biomes are stored at 4x4x4 resolution (one biome per 4-block cube).
 */
public interface BiomeAccess {

    /**
     * Gets the biome at the given block coordinates.
     * Coordinates are internally quantized to 4-block resolution.
     */
    Biome getBiome(int x, int y, int z);

    /**
     * Sets the biome at the given block coordinates.
     */
    void setBiome(int x, int y, int z, Biome biome);
}
