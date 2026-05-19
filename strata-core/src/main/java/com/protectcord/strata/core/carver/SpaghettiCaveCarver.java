package com.protectcord.strata.core.carver;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.carver.Carver;
import com.protectcord.strata.api.carver.CarverContext;
import com.protectcord.strata.api.carver.CarverType;
import com.protectcord.strata.api.carver.CarvingMask;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Creates winding tunnel systems using two perpendicular 2D noise functions for XZ path
 * generation and a 3D noise function for variable tunnel width.
 */
public final class SpaghettiCaveCarver implements Carver {

    private static final NamespacedKey KEY = NamespacedKey.strata("spaghetti_cave");

    private static final StrataBlockState AIR = StrataBlockState.of(NamespacedKey.minecraft("air"));
    private static final StrataBlockState CAVE_AIR = StrataBlockState.of(NamespacedKey.minecraft("cave_air"));
    private static final StrataBlockState BEDROCK = StrataBlockState.of(NamespacedKey.minecraft("bedrock"));

    private static final int MIN_Y = -50;
    private static final int MAX_Y = 40;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 5.0;
    private static final double PATH_FREQ = 0.04;
    private static final double WIDTH_FREQ = 0.03;
    private static final double TUNNEL_THRESHOLD = 0.03;

    private final double probability;

    public SpaghettiCaveCarver(double probability) {
        this.probability = probability;
    }

    @Override
    public NamespacedKey key() {
        return KEY;
    }

    @Override
    public CarverType type() {
        return CarverType.SPAGHETTI_CAVE;
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
                    double pathA = samplePath(worldX, y, worldZ, seed);
                    double pathB = samplePath(worldX, y, worldZ, seed ^ 0xDEADBEEFCAFEL);

                    double tunnelDist = Math.sqrt(pathA * pathA + pathB * pathB);

                    double radius = sampleRadius(worldX, y, worldZ, seed);
                    double normalizedRadius = radius / 16.0;

                    if (tunnelDist < normalizedRadius * TUNNEL_THRESHOLD + TUNNEL_THRESHOLD) {
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

    private static double samplePath(int x, int y, int z, long seed) {
        double sx = x * PATH_FREQ;
        double sy = y * PATH_FREQ;
        double sz = z * PATH_FREQ;

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

    private static double sampleRadius(int x, int y, int z, long seed) {
        long h = NoiseMath.hash(seed ^ 0xABCD1234L, x * 7 + z, y);
        double t = NoiseMath.hashToDouble(h) * 0.5 + 0.5;
        return NoiseMath.lerp(MIN_RADIUS, MAX_RADIUS, t);
    }
}
