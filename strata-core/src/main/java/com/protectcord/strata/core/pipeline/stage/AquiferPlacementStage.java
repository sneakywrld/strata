package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.api.water.AquiferSettings;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Evaluates 3D aquifer noise at Y intervals. Where noise exceeds threshold,
 * places water (above lava threshold) or lava (below lava threshold).
 * Only modifies air blocks inside stone.
 */
public final class AquiferPlacementStage implements PipelineStage {

    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private static final double AQUIFER_THRESHOLD = 0.4;
    private static final int Y_INTERVAL = 4;

    private final NoiseFunction aquiferNoise;

    public AquiferPlacementStage(NoiseFunction aquiferNoise) {
        this.aquiferNoise = aquiferNoise;
    }

    @Override
    public GenerationStage stage() {
        return GenerationStage.AQUIFER_PLACEMENT;
    }

    @Override
    public void generate(GenerationContext context) {
        AquiferSettings settings = context.profile().waterSettings().aquifers();
        if (!settings.enabled()) return;

        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        int lavaThreshold = settings.lavaThreshold();
        StrataBlockState waterBlock = settings.waterBlock();
        StrataBlockState lavaBlock = settings.lavaBlock();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = chunk.minY(); y < context.seaLevel(); y += Y_INTERVAL) {
                    double noise = aquiferNoise.sample(
                            worldX * 0.02,
                            y * 0.03,
                            worldZ * 0.02
                    );

                    if (noise < AQUIFER_THRESHOLD) continue;

                    StrataBlockState fluidBlock = y < lavaThreshold ? lavaBlock : waterBlock;

                    for (int dy = 0; dy < Y_INTERVAL && (y + dy) < chunk.maxY(); dy++) {
                        int blockY = y + dy;
                        StrataBlockState current = chunk.getBlock(worldX, blockY, worldZ);
                        if (!current.equals(AIR)) continue;

                        boolean hasStoneNeighbor = isStone(chunk, worldX - 1, blockY, worldZ)
                                || isStone(chunk, worldX + 1, blockY, worldZ)
                                || isStone(chunk, worldX, blockY - 1, worldZ)
                                || isStone(chunk, worldX, blockY + 1, worldZ)
                                || isStone(chunk, worldX, blockY, worldZ - 1)
                                || isStone(chunk, worldX, blockY, worldZ + 1);

                        if (hasStoneNeighbor) {
                            chunk.setBlock(worldX, blockY, worldZ, fluidBlock);
                        }
                    }
                }
            }
        }
    }

    private static boolean isStone(ProtoChunkAccess chunk, int x, int y, int z) {
        return chunk.getBlock(x, y, z).equals(STONE);
    }
}
