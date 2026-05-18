package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.BlockPalette;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Applies surface blocks (grass, sand, dirt layers, etc.) based on biome.
 * Replaces the top stone layers with biome-appropriate surface materials.
 */
public final class SurfaceBuildingStage implements PipelineStage {

    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState GRASS_BLOCK = StrataBlockState.of(NamespacedKey.minecraft("grass_block"));
    private static final StrataBlockState DIRT = StrataBlockState.of(NamespacedKey.minecraft("dirt"));

    private static final int SURFACE_DEPTH = 5;

    @Override
    public GenerationStage stage() {
        return GenerationStage.SURFACE_BUILDING;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
                if (surfaceY < chunk.minY()) continue;

                Biome biome = chunk.getBiome(worldX, surfaceY, worldZ);
                if (biome == null) continue;

                StrataBlockState topBlock = GRASS_BLOCK;
                StrataBlockState fillBlock = DIRT;

                // Use biome surface palette if available
                if (biome.surfacePalette().isPresent()) {
                    BlockPalette palette = biome.surfacePalette().get();
                    topBlock = palette.primary();
                    if (palette.entries().size() > 1) {
                        fillBlock = palette.entries().get(1).block();
                    }
                }

                // Apply surface depth based on pseudo-random variation
                long hash = NoiseMath.hash(context.seed(), worldX, worldZ);
                int depth = SURFACE_DEPTH + (int) (hash & 3) - 1;

                for (int d = 0; d < depth; d++) {
                    int y = surfaceY - d;
                    if (y < chunk.minY()) break;

                    StrataBlockState current = chunk.getBlock(worldX, y, worldZ);
                    if (!current.equals(STONE)) continue;

                    if (d == 0) {
                        chunk.setBlock(worldX, y, worldZ, topBlock);
                    } else {
                        chunk.setBlock(worldX, y, worldZ, fillBlock);
                    }
                }
            }
        }
    }
}
