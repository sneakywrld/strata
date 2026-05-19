package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Builds waterfall features at detected steep gradient points. Places flowing water
 * down the vertical face and carves a plunge pool basin at the bottom.
 */
public final class WaterfallBuilder {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState GRAVEL = StrataBlockState.of(NamespacedKey.minecraft("gravel"));
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private static final int POOL_RADIUS = 1;
    private static final int POOL_DEPTH = 2;

    public void build(ProtoChunkAccess chunk, WaterfallDetector.WaterfallPoint point) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        int halfWidth = point.width() / 2;

        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            int wx = point.x() + dx;
            if (wx < baseX || wx >= baseX + 16) continue;

            for (int y = point.topY(); y >= point.bottomY(); y--) {
                if (y < chunk.minY() || y >= chunk.maxY()) continue;
                chunk.setBlock(wx, y, point.z(), WATER);
            }
        }

        carvePool(chunk, point.x(), point.bottomY(), point.z());
    }

    private void carvePool(ProtoChunkAccess chunk, int centerX, int bottomY, int centerZ) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        for (int dx = -POOL_RADIUS; dx <= POOL_RADIUS; dx++) {
            for (int dz = -POOL_RADIUS; dz <= POOL_RADIUS; dz++) {
                int wx = centerX + dx;
                int wz = centerZ + dz;

                if (wx < baseX || wx >= baseX + 16) continue;
                if (wz < baseZ || wz >= baseZ + 16) continue;

                for (int dy = 0; dy < POOL_DEPTH; dy++) {
                    int y = bottomY - dy;
                    if (y < chunk.minY()) continue;

                    if (dy == POOL_DEPTH - 1) {
                        chunk.setBlock(wx, y, wz, GRAVEL);
                    } else {
                        chunk.setBlock(wx, y, wz, WATER);
                    }
                }

                if (Math.abs(dx) == POOL_RADIUS || Math.abs(dz) == POOL_RADIUS) {
                    for (int dy = 0; dy < POOL_DEPTH; dy++) {
                        int y = bottomY - dy;
                        if (y < chunk.minY()) continue;
                        chunk.setBlock(wx, y, wz, STONE);
                    }
                }

                int abovePoolY = bottomY + 1;
                if (abovePoolY < chunk.maxY()) {
                    StrataBlockState above = chunk.getBlock(wx, abovePoolY, wz);
                    if (!above.equals(WATER) && !above.equals(AIR)) {
                        chunk.setBlock(wx, abovePoolY, wz, AIR);
                    }
                }
            }
        }
    }
}
