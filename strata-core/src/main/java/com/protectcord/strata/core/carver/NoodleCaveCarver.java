package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarverType;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Creates thin fissures and crevices using high-frequency noise. Produces narrow
 * vertical or horizontal passages 1-3 blocks wide, connecting larger cave systems.
 */
public final class NoodleCaveCarver implements Carver {

    private static final NamespacedKey KEY = NamespacedKey.strata("noodle_cave");

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState CAVE_AIR = StrataBlockState.of(NamespacedKey.minecraft("cave_air"));
    private static final StrataBlockState BEDROCK = StrataBlockState.of(NamespacedKey.minecraft("bedrock"));

    private static final int MIN_Y = -40;
    private static final int MAX_Y = 50;
    private static final double FREQ_PRIMARY = 0.08;
    private static final double FREQ_SECONDARY = 0.06;
    private static final double THICKNESS_THRESHOLD = 0.02;

    private final double probability;

    public NoodleCaveCarver(double probability) {
        this.probability = probability;
    }

    @Override
    public NamespacedKey key() {
        return KEY;
    }

    @Override
    public CarverType type() {
        return CarverType.NOODLE_CAVE;
    }

    @Override
    public boolean carve(CarverContext context, CarvingMask mask) {
        int baseX = context.chunk().blockX();
        int baseZ = context.chunk().blockZ();
        int yMin = Math.max(MIN_Y, context.minY());
        int yMax = Math.min(MAX_Y, context.maxY());
        long seed = context.seed();
        boolean carved = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;

                for (int y = yMin; y < yMax; y++) {
                    double noodleA = sampleNoodle(worldX, y, worldZ, seed, FREQ_PRIMARY);
                    double noodleB = sampleNoodle(worldX, y, worldZ, seed ^ 0x7F4A7C15L, FREQ_SECONDARY);

                    long orientHash = NoiseMath.hash(seed ^ 0xFF00FF00L, worldX >> 4, worldZ >> 4);
                    boolean vertical = (orientHash & 1) == 0;

                    double widthNoise = NoiseMath.hashToDouble(
                            NoiseMath.hash(seed ^ 0x12345678L, worldX, y, worldZ)) * 0.5 + 0.5;
                    double widthThreshold = THICKNESS_THRESHOLD * (1.0 + widthNoise * 2.0);

                    double dist;
                    if (vertical) {
                        dist = Math.abs(noodleA) + Math.abs(noodleB) * 0.5;
                    } else {
                        dist = Math.sqrt(noodleA * noodleA + noodleB * noodleB);
                    }

                    if (dist < widthThreshold) {
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

    private static double sampleNoodle(int x, int y, int z, long seed, double freq) {
        double sx = x * freq;
        double sy = y * freq;
        double sz = z * freq;

        int ix = NoiseMath.fastFloor(sx);
        int iy = NoiseMath.fastFloor(sy);
        int iz = NoiseMath.fastFloor(sz);
        double fx = NoiseMath.smootherstep(sx - ix);
        double fy = NoiseMath.smootherstep(sy - iy);
        double fz = NoiseMath.smootherstep(sz - iz);

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

        return NoiseMath.lerp(c0, c1, fz);
    }
}
