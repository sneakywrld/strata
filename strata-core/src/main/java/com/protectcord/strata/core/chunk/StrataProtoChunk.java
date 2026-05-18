package com.protectcord.strata.core.chunk;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ChunkCoord;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

import java.util.EnumMap;
import java.util.Map;

/**
 * Strata's internal proto-chunk implementation used during generation.
 * Stores block states, biomes, and heightmaps for a 16x16 column.
 */
public final class StrataProtoChunk implements ProtoChunkAccess {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));

    private final ChunkCoord coord;
    private final int minY;
    private final int maxY;
    private final int height;

    // Block storage: flat array indexed by [x + z*16 + (y-minY)*256]
    private final StrataBlockState[] blocks;

    // Biome storage: 4x4x4 resolution, indexed by [bx + bz*4 + (by)*16]
    private final Biome[] biomes;

    // Heightmaps
    private final Map<HeightmapType, int[]> heightmaps;

    public StrataProtoChunk(ChunkCoord coord, int minY, int maxY) {
        this.coord = coord;
        this.minY = minY;
        this.maxY = maxY;
        this.height = maxY - minY;

        this.blocks = new StrataBlockState[16 * 16 * height];
        java.util.Arrays.fill(blocks, AIR);

        int biomeHeight = (height + 3) / 4;
        this.biomes = new Biome[4 * 4 * biomeHeight];

        this.heightmaps = new EnumMap<>(HeightmapType.class);
        for (HeightmapType type : HeightmapType.values()) {
            heightmaps.put(type, new int[16 * 16]);
        }
    }

    @Override
    public ChunkCoord coord() { return coord; }

    @Override
    public int minY() { return minY; }

    @Override
    public int maxY() { return maxY; }

    @Override
    public StrataBlockState getBlock(int x, int y, int z) {
        if (y < minY || y >= maxY) return AIR;
        int idx = (x & 15) + ((z & 15) << 4) + ((y - minY) << 8);
        return blocks[idx];
    }

    @Override
    public void setBlock(int x, int y, int z, StrataBlockState state) {
        if (y < minY || y >= maxY) return;
        int lx = x & 15;
        int lz = z & 15;
        int idx = lx + (lz << 4) + ((y - minY) << 8);
        blocks[idx] = state;

        // Update heightmaps
        if (!state.equals(AIR)) {
            for (var entry : heightmaps.entrySet()) {
                int hmIdx = lx + lz * 16;
                if (y >= entry.getValue()[hmIdx]) {
                    entry.getValue()[hmIdx] = y + 1;
                }
            }
        }
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        int bx = (x & 15) >> 2;
        int by = (y - minY) >> 2;
        int bz = (z & 15) >> 2;
        int biomeHeight = (height + 3) / 4;
        if (by < 0) by = 0;
        if (by >= biomeHeight) by = biomeHeight - 1;
        int idx = bx + bz * 4 + by * 16;
        return biomes[idx];
    }

    @Override
    public void setBiome(int x, int y, int z, Biome biome) {
        int bx = (x & 15) >> 2;
        int by = (y - minY) >> 2;
        int bz = (z & 15) >> 2;
        int biomeHeight = (height + 3) / 4;
        if (by < 0) by = 0;
        if (by >= biomeHeight) by = biomeHeight - 1;
        int idx = bx + bz * 4 + by * 16;
        biomes[idx] = biome;
    }

    @Override
    public int getHeight(int x, int z) {
        return getHeight(HeightmapType.WORLD_SURFACE, x, z);
    }

    @Override
    public int getHeight(HeightmapType type, int x, int z) {
        return heightmaps.get(type)[(x & 15) + (z & 15) * 16];
    }

    @Override
    public void markForLightUpdate(int x, int y, int z) {
        // Tracked by the lighting stage
    }
}
