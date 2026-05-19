package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarverType;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Creates large open chambers using 3D low-frequency noise, producing Swiss cheese-style
 * cave systems. Carves spheroidal voids where noise exceeds a threshold.
 */
public final class CheeseCaveCarver implements Carver {

    private static final NamespacedKey KEY = NamespacedKey.strata("cheese_cave");

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState CAVE_AIR = StrataBlockState.of(NamespacedKey.minecraft("cave_air"));
    private static final StrataBlockState BEDROCK = StrataBlockState.of(NamespacedKey.minecraft("bedrock"));

    private static final int MIN_Y = -60;
    private static final int MAX_Y = 50;
    private static final double NOISE_THRESHOLD = 0.5;
    private static final double FREQ_X = 0.02;
    private static final double FREQ_Y = 0.03;
    private static final double FREQ_Z = 0.02;

    private final double probability;

    public CheeseCaveCarver(double probability) {
        this.probability = probability;
    }

    @Override
    public NamespacedKey key() {
        return KEY;
    }

    @Override
    public CarverType type() {
        return CarverType.CHEESE_CAVE;
    }

    @Override
    public boolean carve(CarverContext context, CarvingMask mask) {
        int baseX = context.chunk().blockX();
        int baseZ = context.chunk().blockZ();
        int yMin = Math.max(MIN_Y, context.minY());
        int yMax = Math.min(MAX_Y, context.maxY());
        boolean carved = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = yMin; y < yMax; y++) {
                    double noise = sampleNoise3D(worldX, y, worldZ, context.seed());

                    if (noise > NOISE_THRESHOLD) {
                        StrataBlockState current = context.blocks().getBlock(worldX, y, worldZ);
                        if (current.equals(BEDROCK)) continue;

                        StrataBlockState replacement = (y < context.seaLevel()) ? CAVE_AIR : AIR;
                        context.blocks().setBlock(worldX, y, worldZ, replacement);
                        mask.set(x, y, z);
                        carved = true;
                    }
                }
            }
        }

        return carved;
    }

    @Override
    public double probability() {
        return probability;
    }

    private static double sampleNoise3D(int x, int y, int z, long seed) {
        double sx = x * FREQ_X;
        double sy = y * FREQ_Y;
        double sz = z * FREQ_Z;

        long h1 = NoiseMath.hash(seed, (int) (sx * 1000), (int) (sy * 1000), (int) (sz * 1000));
        long h2 = NoiseMath.hash(seed ^ 0x9E3779B97F4A7C15L,
                NoiseMath.fastFloor(sx), NoiseMath.fastFloor(sy));

        double base = NoiseMath.hashToDouble(h1);
        double detail = NoiseMath.hashToDouble(h2) * 0.3;

        int ix = NoiseMath.fastFloor(sx);
        int iy = NoiseMath.fastFloor(sy);
        int iz = NoiseMath.fastFloor(sz);
        double fx = sx - ix;
        double fy = sy - iy;
        double fz = sz - iz;
        fx = NoiseMath.smootherstep(fx);
        fy = NoiseMath.smootherstep(fy);
        fz = NoiseMath.smootherstep(fz);

        double c000 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix, iy, iz));
        double c100 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix + 1, iy, iz));
        double c010 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix, iy + 1, iz));
        double c110 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix + 1, iy + 1, iz));
        double c001 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix, iy, iz + 1));
        double c101 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix + 1, iy, iz + 1));
        double c011 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix, iy + 1, iz + 1));
        double c111 = NoiseMath.hashToDouble(NoiseMath.hash(seed, ix + 1, iy + 1, iz + 1));

        double c00 = NoiseMath.lerp(c000, c100, fx);
        double c10 = NoiseMath.lerp(c010, c110, fx);
        double c01 = NoiseMath.lerp(c001, c101, fx);
        double c11 = NoiseMath.lerp(c011, c111, fx);

        double c0 = NoiseMath.lerp(c00, c10, fy);
        double c1 = NoiseMath.lerp(c01, c11, fy);

        double value = NoiseMath.lerp(c0, c1, fz);

        return (value + base * 0.2 + detail) * 0.5 + 0.5;
    }
}
