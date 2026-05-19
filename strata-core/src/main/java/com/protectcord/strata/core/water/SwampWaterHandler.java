package com.protectcord.strata.core.water;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.chunk.HeightmapType;
import com.protectcord.strata.api.chunk.ProtoChunkAccess;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Handles swamp biome water table elevation. Raises the water level 1-3 blocks above
 * sea level, placing shallow water pools with mud bottoms and lily pads on the surface.
 * Pool placement is controlled by noise for natural irregular patterns.
 */
public final class SwampWaterHandler {

    private static final StrataBlockState WATER = StrataBlockState.of(NamespacedKey.minecraft("water"));
    private static final StrataBlockState MUD = StrataBlockState.of(NamespacedKey.minecraft("mud"));
    private static final StrataBlockState LILY_PAD = StrataBlockState.of(NamespacedKey.minecraft("lily_pad"));
    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));

    private static final double POOL_FREQ = 0.05;
    private static final double POOL_THRESHOLD = 0.3;
    private static final int MIN_RAISE = 1;
    private static final int MAX_RAISE = 3;
    private static final double LILY_PAD_CHANCE = 0.15;
    private static final long SWAMP_SALT = 0x5A3BEED5A3BEEDL;

    public void apply(ProtoChunkAccess chunk, int seaLevel) {
        int baseX = chunk.coord().blockX();
        int baseZ = chunk.coord().blockZ();
        long chunkSeed = ((long) chunk.coord().x() * 341873128712L)
                ^ ((long) chunk.coord().z() * 132897987541L);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                double poolNoise = samplePoolNoise(worldX, worldZ, chunkSeed);
                if (poolNoise < POOL_THRESHOLD) continue;

                int surfaceY = chunk.getHeight(HeightmapType.WORLD_SURFACE, worldX, worldZ) - 1;
                if (surfaceY < seaLevel || surfaceY > seaLevel + MAX_RAISE + 2) continue;

                double intensity = (poolNoise - POOL_THRESHOLD) / (1.0 - POOL_THRESHOLD);
                int waterRaise = MIN_RAISE + (int) (intensity * (MAX_RAISE - MIN_RAISE));
                int raisedLevel = seaLevel + waterRaise;

                if (surfaceY > raisedLevel) continue;

                chunk.setBlock(worldX, surfaceY, worldZ, MUD);

                for (int y = surfaceY + 1; y <= raisedLevel && y < chunk.maxY(); y++) {
                    chunk.setBlock(worldX, y, worldZ, WATER);
                }

                int lilyY = raisedLevel + 1;
                if (lilyY < chunk.maxY()) {
                    long lilyHash = NoiseMath.hash(chunkSeed, worldX, worldZ);
                    double lilyChance = NoiseMath.hashToDouble(lilyHash) * 0.5 + 0.5;
                    if (lilyChance < LILY_PAD_CHANCE) {
                        StrataBlockState above = chunk.getBlock(worldX, lilyY, worldZ);
                        if (above.equals(AIR)) {
                            chunk.setBlock(worldX, lilyY, worldZ, LILY_PAD);
                        }
                    }
                }
            }
        }
    }

    private static double samplePoolNoise(int x, int z, long seed) {
        double sx = x * POOL_FREQ;
        double sz = z * POOL_FREQ;

        int ix = NoiseMath.fastFloor(sx);
        int iz = NoiseMath.fastFloor(sz);
        double fx = NoiseMath.smootherstep(sx - ix);
        double fz = NoiseMath.smootherstep(sz - iz);

        double n00 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ SWAMP_SALT, ix, iz));
        double n10 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ SWAMP_SALT, ix + 1, iz));
        double n01 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ SWAMP_SALT, ix, iz + 1));
        double n11 = NoiseMath.hashToDouble(NoiseMath.hash(seed ^ SWAMP_SALT, ix + 1, iz + 1));

        double nx0 = NoiseMath.lerp(n00, n10, fx);
        double nx1 = NoiseMath.lerp(n01, n11, fx);

        return NoiseMath.lerp(nx0, nx1, fz) * 0.5 + 0.5;
    }
}
