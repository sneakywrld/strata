package com.protectcord.strata.noise.math;

/**
 * Mathematical utilities for noise generation.
 */
public final class NoiseMath {

    private NoiseMath() {}

    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    public static double smootherstep(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    /**
     * High-quality hash for coordinate-based seeding.
     */
    public static long hash(long seed, int x, int y) {
        long h = seed;
        h ^= x * 0x6C62272E07BB0142L;
        h ^= y * 0x94D049BB133111EBL;
        h ^= h >>> 32;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }

    public static long hash(long seed, int x, int y, int z) {
        long h = hash(seed, x, y);
        h ^= z * 0x517CC1B727220A95L;
        h ^= h >>> 32;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 31;
        return h;
    }

    /**
     * Converts a hash to a double in the range [-1, 1].
     */
    public static double hashToDouble(long hash) {
        return (hash & 0x1FFFFFFFFFFFFFL) * (2.0 / 0x1FFFFFFFFFFFFFL) - 1.0;
    }

    /**
     * Gradient dot product for 2D Perlin-type noise.
     */
    public static double grad2(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -v : v);
    }

    /**
     * Gradient dot product for 3D Perlin-type noise.
     */
    public static double grad3(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -v : v);
    }
}
