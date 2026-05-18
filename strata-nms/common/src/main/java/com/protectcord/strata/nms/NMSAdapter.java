package com.protectcord.strata.nms;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;

/**
 * Version-specific NMS adapter interface.
 * Each supported Minecraft version provides an implementation via ServiceLoader.
 */
public interface NMSAdapter {

    /**
     * Returns which NMS version this adapter supports.
     */
    NMSVersion version();

    /**
     * Converts a Strata block state to the native NMS block state.
     */
    Object toNativeBlockState(StrataBlockState state);

    /**
     * Converts a native NMS block state to a Strata block state.
     */
    StrataBlockState fromNativeBlockState(Object nativeState);

    /**
     * Injects a Strata biome into the server's biome registry.
     */
    void registerBiome(Biome biome);

    /**
     * Returns the block state mapper for bulk operations.
     */
    BlockStateMapper blockStateMapper();

    /**
     * Returns the chunk accessor for reading/writing native chunks.
     */
    ChunkAccessor chunkAccessor();

    /**
     * Returns the lighting engine for recalculating light levels.
     */
    LightingEngine lightingEngine();

    /**
     * Returns the heightmap writer for updating native heightmaps.
     */
    HeightmapWriter heightmapWriter();
}
