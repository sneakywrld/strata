package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.terrain.TerrainSettings;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Shapes 3D terrain by sampling density functions.
 * Positive density = solid (stone), negative density = air (or water below sea level).
 */
public final class TerrainShapingStage implements PipelineStage {

    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState LAVA = StrataBlockState.of(NamespacedKey.minecraft("lava"));

    private final NoiseFunction densityNoise;

    public TerrainShapingStage(NoiseFunction densityNoise) {
        this.densityNoise = densityNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.TERRAIN_SHAPING;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        TerrainSettings terrain = context.profile().terrainSettings();
        int seaLevel = terrain.seaLevel();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = chunk.minY(); y < chunk.maxY(); y++) {
                    double density = densityNoise.sample(
                            worldX * 0.01 * terrain.heightScale(),
                            y * 0.015,
                            worldZ * 0.01 * terrain.heightScale()
                    );

                    // Height-based density bias: more solid below, more air above
                    double heightBias = (seaLevel - y) * 0.02 + terrain.baseHeightOffset();
                    density += heightBias;

                    if (density > 0) {
                        chunk.setBlock(worldX, y, worldZ, STONE);
                    } else if (y < seaLevel) {
                        chunk.setBlock(worldX, y, worldZ, WATER);
                    }
                }
            }
        }
    }
}
