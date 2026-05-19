package com.protectcord.strata.core.water;

import com.protectcord.strata.api.biome.Biome;
import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Fills detected lake basins with water, placing appropriate bottom materials
 * and surface vegetation based on biome temperature.
 */
public final class LakeFiller {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState CLAY = StrataBlockState.of(NamespacedKey.minecraft("clay"));
    private static final StrataBlockState MUD = StrataBlockState.of(NamespacedKey.minecraft("mud"));
    private static final StrataBlockState SAND = StrataBlockState.of(NamespacedKey.minecraft("sand"));
    private static final StrataBlockState LILY_PAD = StrataBlockState.of(NamespacedKey.minecraft("lily_pad"));

    private static final double WARM_TEMP_THRESHOLD = 0.5;

    public void fill(ProtoChunkAccess chunk, LakeDetector.LakeRegion lake, int seaLevel) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();

        int worldCenterX = baseX + lake.centerX();
        int worldCenterZ = baseZ + lake.centerZ();
        int radiusSq = lake.radius() * lake.radius();

        for (int dx = -lake.radius(); dx <= lake.radius(); dx++) {
            for (int dz = -lake.radius(); dz <= lake.radius(); dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > radiusSq) continue;

                int wx = worldCenterX + dx;
                int wz = worldCenterZ + dz;

                if (wx < baseX || wx >= baseX + 16) continue;
                if (wz < baseZ || wz >= baseZ + 16) continue;

                double normalizedDist = Math.sqrt((double) distSq / radiusSq);
                int localDepth = (int) (lake.depth() * (1.0 - normalizedDist * normalizedDist));
                if (localDepth <= 0) continue;

                int bottomY = lake.surfaceY() - localDepth;

                boolean isEdge = normalizedDist > 0.8;

                for (int y = lake.surfaceY(); y >= bottomY && y >= chunk.minY(); y--) {
                    if (y == bottomY) {
                        chunk.setBlock(wx, y, wz, isEdge ? SAND : selectBottomMaterial(normalizedDist));
                    } else {
                        chunk.setBlock(wx, y, wz, WATER);
                    }
                }

                if (isEdge && lake.surfaceY() + 1 < chunk.maxY()) {
                    chunk.setBlock(wx, lake.surfaceY() + 1, wz, SAND);
                }

                Biome biome = chunk.getBiome(wx, lake.surfaceY(), wz);
                if (biome != null && isWarmBiome(biome) && !isEdge && lake.surfaceY() + 1 < chunk.maxY()) {
                    if ((dx + dz) % 3 == 0) {
                        chunk.setBlock(wx, lake.surfaceY() + 1, wz, LILY_PAD);
                    }
                }
            }
        }
    }

    private static StrataBlockState selectBottomMaterial(double normalizedDist) {
        if (normalizedDist < 0.4) {
            return MUD;
        }
        return CLAY;
    }

    private static boolean isWarmBiome(Biome biome) {
        return biome.effects().temperature() >= WARM_TEMP_THRESHOLD;
    }
}
