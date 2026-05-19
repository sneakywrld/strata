package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.pipeline.GenerationContext;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Shapes ocean floor terrain based on distance from coastline. Produces continental
 * shelf areas with gradual sand/gravel slopes, flat deep ocean floors with clay/gravel,
 * and deep trenches that cut below Y=10.
 */
public final class OceanFloorBuilder {

    private static final StrataBlockState SAND = StrataBlockState.of(NamespacedKey.minecraft("sand"));
    private static final StrataBlockState GRAVEL = StrataBlockState.of(NamespacedKey.minecraft("gravel"));
    private static final StrataBlockState CLAY = StrataBlockState.of(NamespacedKey.minecraft("clay"));
    private static final StrataBlockState STONE = StrataBlockState.of(NamespacedKey.minecraft("stone"));
    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));

    private static final int SHELF_DEPTH = 15;
    private static final int DEEP_OCEAN_DEPTH = 45;
    private static final int TRENCH_MIN_Y = 10;
    private static final double TRENCH_FREQ = 0.003;
    private static final double TRENCH_THRESHOLD = 0.75;
    private static final long TRENCH_SALT = 0xCEA4F100DEADL;

    public void buildFloor(ProtoChunkAccess chunk, int seaLevel, GenerationContext ctx) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        long seed = ctx.seed();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                int surfaceY = chunk.getHeight(HeightmapType.OCEAN_FLOOR, worldX, worldZ) - 1;
                if (surfaceY >= seaLevel) continue;

                int waterDepth = seaLevel - surfaceY;
                long floorHash = NoiseMath.hash(seed, worldX, worldZ);
                double floorNoise = NoiseMath.hashToDouble(floorHash) * 0.5 + 0.5;

                if (waterDepth <= SHELF_DEPTH) {
                    buildShelf(chunk, worldX, surfaceY, worldZ, floorNoise);
                } else {
                    buildDeepFloor(chunk, worldX, surfaceY, worldZ, floorNoise);
                }

                double trenchNoise = sampleTrenchNoise(worldX, worldZ, seed);
                if (trenchNoise > TRENCH_THRESHOLD) {
                    carveTrench(chunk, worldX, surfaceY, worldZ, trenchNoise, seed);
                }

                fillWaterColumn(chunk, surfaceY, seaLevel, worldX, worldZ);
            }
        }
    }

    private void buildShelf(ProtoChunkAccess chunk, int x, int surfaceY, int z, double noise) {
        int depth = 2 + (int) (noise * 3);
        for (int d = 0; d < depth && surfaceY - d >= chunk.minY(); d++) {
            chunk.setBlock(x, surfaceY - d, z, d == 0 ? SAND : (noise > 0.6 ? GRAVEL : SAND));
        }
    }

    private void buildDeepFloor(ProtoChunkAccess chunk, int x, int surfaceY, int z, double noise) {
        int depth = 3 + (int) (noise * 2);
        for (int d = 0; d < depth && surfaceY - d >= chunk.minY(); d++) {
            StrataBlockState material = switch (d) {
                case 0 -> noise > 0.5 ? CLAY : GRAVEL;
                case 1 -> CLAY;
                default -> STONE;
            };
            chunk.setBlock(x, surfaceY - d, z, material);
        }
    }

    private void carveTrench(ProtoChunkAccess chunk, int x, int surfaceY, int z,
                             double trenchNoise, long seed) {
        double intensity = (trenchNoise - TRENCH_THRESHOLD) / (1.0 - TRENCH_THRESHOLD);
        int trenchDepth = (int) (intensity * (surfaceY - TRENCH_MIN_Y));
        int trenchBottom = Math.max(surfaceY - trenchDepth, TRENCH_MIN_Y);

        for (int y = surfaceY; y >= trenchBottom; y--) {
            if (y < chunk.minY()) break;

            long blockHash = NoiseMath.hash(seed ^ TRENCH_SALT, x, y, z);
            double wallNoise = NoiseMath.hashToDouble(blockHash) * 0.5 + 0.5;

            if (wallNoise > 0.3) {
                chunk.setBlock(x, y, z, WATER);
            }
        }

        if (trenchBottom >= chunk.minY()) {
            chunk.setBlock(x, trenchBottom, z, GRAVEL);
        }
    }

    private void fillWaterColumn(ProtoChunkAccess chunk, int floorY, int seaLevel, int x, int z) {
        for (int y = floorY + 1; y <= seaLevel && y < chunk.maxY(); y++) {
            chunk.setBlock(x, y, z, WATER);
        }
    }

    private static double sampleTrenchNoise(int x, int z, long seed) {
        double sx = x * TRENCH_FREQ;
        double sz = z * TRENCH_FREQ;

        int ix = NoiseMath.fastFloor(sx);
        int iz = NoiseMath.fastFloor(sz);
        double fx = NoiseMath.smootherstep(sx - ix);
        double fz = NoiseMath.smootherstep(sz - iz);

        double n00 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ TRENCH_SALT, ix, iz));
        double n10 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ TRENCH_SALT, ix + 1, iz));
        double n01 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ TRENCH_SALT, ix, iz + 1));
        double n11 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ TRENCH_SALT, ix + 1, iz + 1));

        double nx0 = NoiseMath.lerp(n00, n10, fx);
        double nx1 = NoiseMath.lerp(n01, n11, fx);

        return NoiseMath.lerp(nx0, nx1, fz) * 0.5 + 0.5;
    }
}
