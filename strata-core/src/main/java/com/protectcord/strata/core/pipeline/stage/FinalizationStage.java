package com.protectcord.strata.core.pipeline.stage;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.api.pipeline.GenerationStage;
import com.protectcord.strata.core.pipeline.PipelineStage;

/**
 * Final pipeline stage. Computes heightmaps by scanning columns top-down:
 * WORLD_SURFACE = first non-air, OCEAN_FLOOR = first solid (non-fluid),
 * MOTION_BLOCKING = first solid-or-fluid. Marks chunk generation complete.
 */
public final class FinalizationStage implements PipelineStage {

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    @Override
    public GenerationStage stage() {
        return GenerationStage.FINALIZATION;
    }

    @Override
    public void generate(GenerationContext context) {
        ProtoChunkAccess chunk = context.chunk();
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        int minY = chunk.minY();
        int maxY = chunk.maxY();

        int[] worldSurface = new int[16 * 16];
        int[] oceanFloor = new int[16 * 16];
        int[] motionBlocking = new int[16 * 16];
        int[] motionBlockingNoLeaves = new int[16 * 16];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int idx = x + z * 16;

                boolean foundSurface = false;
                boolean foundFloor = false;
                boolean foundMotion = false;
                boolean foundMotionNoLeaves = false;

                for (int y = maxY - 1; y >= minY; y--) {
                    StrataBlockState block = chunk.getBlock(worldX, y, worldZ);
                    if (block.equals(AIR)) continue;

                    boolean isFluid = isFluid(block);
                    boolean isSolid = !isFluid;
                    boolean isLeaf = isLeaves(block);

                    if (!foundSurface) {
                        worldSurface[idx] = y + 1;
                        foundSurface = true;
                    }

                    if (!foundMotion && (isSolid || isFluid)) {
                        motionBlocking[idx] = y + 1;
                        foundMotion = true;
                    }

                    if (!foundMotionNoLeaves && (isSolid || isFluid) && !isLeaf) {
                        motionBlockingNoLeaves[idx] = y + 1;
                        foundMotionNoLeaves = true;
                    }

                    if (!foundFloor && isSolid) {
                        oceanFloor[idx] = y + 1;
                        foundFloor = true;
                    }

                    if (foundSurface && foundFloor && foundMotion && foundMotionNoLeaves) break;
                }
            }
        }

        context.put("heightmap_world_surface", worldSurface);
        context.put("heightmap_ocean_floor", oceanFloor);
        context.put("heightmap_motion_blocking", motionBlocking);
        context.put("heightmap_motion_blocking_no_leaves", motionBlockingNoLeaves);
        context.put("generation_complete", true);
    }

    private static boolean isFluid(StrataBlockState block) {
        String key = block.blockId().key();
        return key.equals("water") || key.equals("lava");
    }

    private static boolean isLeaves(StrataBlockState block) {
        return block.blockId().key().contains("leaves");
    }
}
