package com.protectcord.strata.core.water;

import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Determines underground fluid type at any position using 3D noise-based aquifer
 * boundaries. Water fills aquifers above Y=-20, and lava fills aquifers below.
 */
public final class AquiferEngine {

    public enum FluidType {
        NONE,
        WATER,
        LAVA
    }

    private static final int LAVA_THRESHOLD_Y = -20;
    private static final double AQUIFER_FREQ = 0.015;
    private static final double AQUIFER_THRESHOLD = 0.4;
    private static final long AQUIFER_SALT = 0xABCDABCDABCDABCDL;

    public FluidType getFluidAt(int x, int y, int z, long seed) {
        double noise = sampleAquiferNoise(x, y, z, seed);

        if (noise < AQUIFER_THRESHOLD) {
            return FluidType.NONE;
        }

        return y <= LAVA_THRESHOLD_Y ? FluidType.LAVA : FluidType.WATER;
    }

    private static double sampleAquiferNoise(int x, int y, int z, long seed) {
        double sx = x * AQUIFER_FREQ;
        double sy = y * AQUIFER_FREQ;
        double sz = z * AQUIFER_FREQ;

        int ix = NoiseMath.fastFloor(sx);
        int iy = NoiseMath.fastFloor(sy);
        int iz = NoiseMath.fastFloor(sz);
        double fx = NoiseMath.smootherstep(sx - ix);
        double fy = NoiseMath.smootherstep(sy - iy);
        double fz = NoiseMath.smootherstep(sz - iz);

        long saltedSeed = seed ^ AQUIFER_SALT;

        double c000 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix, iy, iz));
        double c100 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix + 1, iy, iz));
        double c010 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix, iy + 1, iz));
        double c110 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix + 1, iy + 1, iz));
        double c001 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix, iy, iz + 1));
        double c101 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix + 1, iy, iz + 1));
        double c011 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix, iy + 1, iz + 1));
        double c111 = NoiseMath.hashToDouble(NoiseMath.hash(saltedSeed, ix + 1, iy + 1, iz + 1));

        double c00 = NoiseMath.lerp(c000, c100, fx);
        double c10 = NoiseMath.lerp(c010, c110, fx);
        double c01 = NoiseMath.lerp(c001, c101, fx);
        double c11 = NoiseMath.lerp(c011, c111, fx);

        double c0 = NoiseMath.lerp(c00, c10, fy);
        double c1 = NoiseMath.lerp(c01, c11, fy);

        return NoiseMath.lerp(c0, c1, fz) * 0.5 + 0.5;
    }
}
