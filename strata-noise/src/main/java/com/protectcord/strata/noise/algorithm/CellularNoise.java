package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.noise.math.NoiseMath;

/**
 * Cellular (Voronoi/Worley) noise.
 * Produces cell-like patterns useful for biome boundaries, rock textures, etc.
 */
public final class CellularNoise extends AbstractNoiseGenerator {

    public enum ReturnType {
        /** Distance to nearest cell center. */
        DISTANCE,
        /** Distance to second-nearest minus nearest (edge detection). */
        EDGE_DISTANCE,
        /** Hash of the nearest cell (constant per cell). */
        CELL_VALUE
    }

    private final ReturnType returnType;

    public CellularNoise(NamespacedKey key, long seed, ReturnType returnType) {
        super(key, seed);
        this.returnType = returnType;
    }

    public CellularNoise(NamespacedKey key, long seed) {
        this(key, seed, ReturnType.DISTANCE);
    }

    @Override
    public double sample(double x, double z) {
        int xi = NoiseMath.fastFloor(x);
        int zi = NoiseMath.fastFloor(z);

        double minDist = Double.MAX_VALUE;
        double secondDist = Double.MAX_VALUE;
        long closestHash = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = xi + dx;
                int cz = zi + dz;
                long h = NoiseMath.hash(seed, cx, cz, 0);

                // Random point within cell
                double px = cx + ((h & 0xFFFFL) / 65536.0);
                double pz = cz + (((h >>> 16) & 0xFFFFL) / 65536.0);

                double dist = (x - px) * (x - px) + (z - pz) * (z - pz);
                if (dist < minDist) {
                    secondDist = minDist;
                    minDist = dist;
                    closestHash = h;
                } else if (dist < secondDist) {
                    secondDist = dist;
                }
            }
        }

        return switch (returnType) {
            case DISTANCE -> Math.sqrt(minDist) * 2.0 - 1.0;
            case EDGE_DISTANCE -> (Math.sqrt(secondDist) - Math.sqrt(minDist)) * 2.0 - 1.0;
            case CELL_VALUE -> NoiseMath.hashToDouble(closestHash);
        };
    }

    @Override
    public double sample(double x, double y, double z) {
        int xi = NoiseMath.fastFloor(x);
        int yi = NoiseMath.fastFloor(y);
        int zi = NoiseMath.fastFloor(z);

        double minDist = Double.MAX_VALUE;
        double secondDist = Double.MAX_VALUE;
        long closestHash = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int cx = xi + dx, cy = yi + dy, cz = zi + dz;
                    long h = NoiseMath.hash(seed, cx, cy, cz);

                    double px = cx + ((h & 0xFFFFL) / 65536.0);
                    double py = cy + (((h >>> 16) & 0xFFFFL) / 65536.0);
                    double pz = cz + (((h >>> 32) & 0xFFFFL) / 65536.0);

                    double dist = (x-px)*(x-px) + (y-py)*(y-py) + (z-pz)*(z-pz);
                    if (dist < minDist) {
                        secondDist = minDist;
                        minDist = dist;
                        closestHash = h;
                    } else if (dist < secondDist) {
                        secondDist = dist;
                    }
                }
            }
        }

        return switch (returnType) {
            case DISTANCE -> Math.sqrt(minDist) * 2.0 - 1.0;
            case EDGE_DISTANCE -> (Math.sqrt(secondDist) - Math.sqrt(minDist)) * 2.0 - 1.0;
            case CELL_VALUE -> NoiseMath.hashToDouble(closestHash);
        };
    }
}
