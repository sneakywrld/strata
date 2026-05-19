package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;

import java.util.List;
import java.util.Random;

/**
 * Orchestrates all registered carvers for a chunk, managing mask creation
 * and post-carve fluid placement.
 */
public final class CarverEngine {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState CAVE_AIR = StrataBlockState.of(NamespacedKey.minecraft("cave_air"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState LAVA = StrataBlockState.of(NamespacedKey.minecraft("lava"));

    private static final int LAVA_LEVEL = -10;

    public void carveChunk(ProtoChunkAccess chunk, GenerationContext ctx, List<Carver> carvers) {
        CarvingMask mask = new SimpleCarvingMask(chunk.minY(), chunk.maxY());

        CarverContext carverCtx = new CarverContext(
                chunk,
                chunk.coord(),
                ctx.seed(),
                ctx.seaLevel(),
                chunk.minY(),
                chunk.maxY()
        );

        Random random = new Random(carverCtx.chunkSeed());

        for (Carver carver : carvers) {
            if (random.nextDouble() < carver.probability()) {
                carver.carve(carverCtx, mask);
            }
        }

        applyFluidFill(chunk, mask);
        ctx.put("carving_mask", mask);
    }

    private void applyFluidFill(ProtoChunkAccess chunk, CarvingMask mask) {
        if (mask.carvedCount() == 0) return;

        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.minY(); y < chunk.maxY(); y++) {
                    if (!mask.get(x, y, z)) continue;

                    StrataBlockState current = chunk.getBlock(baseX + x, y, baseZ + z);
                    if (!current.equals(AIR) && !current.equals(CAVE_AIR)) continue;

                    if (y <= LAVA_LEVEL) {
                        chunk.setBlock(baseX + x, y, baseZ + z, LAVA);
                    } else if (hasAdjacentWater(chunk, baseX + x, y, baseZ + z)) {
                        chunk.setBlock(baseX + x, y, baseZ + z, WATER);
                    }
                }
            }
        }
    }

    private boolean hasAdjacentWater(ProtoChunkAccess chunk, int x, int y, int z) {
        return chunk.getBlock(x - 1, y, z).equals(WATER)
                || chunk.getBlock(x + 1, y, z).equals(WATER)
                || chunk.getBlock(x, y, z - 1).equals(WATER)
                || chunk.getBlock(x, y, z + 1).equals(WATER)
                || chunk.getBlock(x, y + 1, z).equals(WATER);
    }
}
