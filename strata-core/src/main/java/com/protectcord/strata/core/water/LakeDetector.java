package com.protectcord.strata.core.water;

import com.protectcord.strata.noise.math.NoiseMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects terrain depressions suitable for lake generation by finding local minima
 * in the heightmap that are surrounded by higher terrain on all sides.
 */
public final class LakeDetector {

    public record LakeRegion(int centerX, int centerZ, int radius, int surfaceY, int depth) {}

    private static final int MIN_RADIUS = 3;
    private static final int MAX_RADIUS = 7;
    private static final int MIN_DEPTH = 2;
    private static final int MAX_DEPTH = 6;
    private static final int SCAN_STEP = 4;

    public List<LakeRegion> detect(int[] heightmap, int chunkSize, long seed) {
        List<LakeRegion> lakes = new ArrayList<>();

        for (int x = SCAN_STEP; x < chunkSize - SCAN_STEP; x += SCAN_STEP) {
            for (int z = SCAN_STEP; z < chunkSize - SCAN_STEP; z += SCAN_STEP) {
                int centerHeight = heightmap[x + z * chunkSize];

                if (!isLocalMinimum(heightmap, chunkSize, x, z, centerHeight)) continue;

                int surroundingAvg = averageSurroundingHeight(heightmap, chunkSize, x, z);
                int depression = surroundingAvg - centerHeight;

                if (depression < MIN_DEPTH) continue;

                long hash = NoiseMath.hash(seed, x, z);
                double sizeFactor = NoiseMath.hashToDouble(hash) * 0.5 + 0.5;
                int radius = MIN_RADIUS + (int) (sizeFactor * (MAX_RADIUS - MIN_RADIUS));
                int depth = Math.min(depression, MIN_DEPTH + (int) (sizeFactor * (MAX_DEPTH - MIN_DEPTH)));

                radius = Math.min(radius, Math.min(x, Math.min(z,
                        Math.min(chunkSize - 1 - x, chunkSize - 1 - z))));

                if (radius >= MIN_RADIUS) {
                    lakes.add(new LakeRegion(x, z, radius, centerHeight + depth, depth));
                }
            }
        }

        return lakes;
    }

    private static boolean isLocalMinimum(int[] heightmap, int chunkSize, int x, int z, int height) {
        for (int dx = -SCAN_STEP; dx <= SCAN_STEP; dx += SCAN_STEP) {
            for (int dz = -SCAN_STEP; dz <= SCAN_STEP; dz += SCAN_STEP) {
                if (dx == 0 && dz == 0) continue;
                int nx = x + dx;
                int nz = z + dz;
                if (nx < 0 || nx >= chunkSize || nz < 0 || nz >= chunkSize) continue;
                if (heightmap[nx + nz * chunkSize] <= height) return false;
            }
        }
        return true;
    }

    private static int averageSurroundingHeight(int[] heightmap, int chunkSize, int x, int z) {
        int sum = 0;
        int count = 0;
        for (int dx = -SCAN_STEP; dx <= SCAN_STEP; dx += SCAN_STEP) {
            for (int dz = -SCAN_STEP; dz <= SCAN_STEP; dz += SCAN_STEP) {
                if (dx == 0 && dz == 0) continue;
                int nx = x + dx;
                int nz = z + dz;
                if (nx < 0 || nx >= chunkSize || nz < 0 || nz >= chunkSize) continue;
                sum += heightmap[nx + nz * chunkSize];
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }
}
