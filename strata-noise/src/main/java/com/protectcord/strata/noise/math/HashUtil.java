package com.protectcord.strata.noise.math;

/**
 * Fast hash functions for noise coordinate seeding.
 *
 * <p>Uses xxHash-style bit mixing to produce well-distributed hash values
 * from integer coordinates and a seed. These are designed for speed in
 * inner loops, not for cryptographic purposes.</p>
 */
public final class HashUtil {

    private static final long PRIME1 = 0x9E3779B185EBCA87L;
    private static final long PRIME2 = 0xC2B2AE3D27D4EB4FL;
    private static final long PRIME3 = 0x165667B19E3779F9L;
    private static final long PRIME4 = 0x85EBCA77C2B2AE63L;
    private static final long PRIME5 = 0x27D4EB2F165667C5L;

    private HashUtil() {}

    /**
     * Hashes a 2D coordinate with a seed, producing a well-distributed long.
     */
    public static long hash2D(long seed, int x, int z) {
        long h = seed + PRIME5;
        h += 16;

        long k1 = (long) x * PRIME2;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= PRIME1;
        h ^= k1;
        h = Long.rotateLeft(h, 27) * PRIME1 + PRIME4;

        long k2 = (long) z * PRIME2;
        k2 = Long.rotateLeft(k2, 31);
        k2 *= PRIME1;
        h ^= k2;
        h = Long.rotateLeft(h, 27) * PRIME1 + PRIME4;

        return avalanche(h);
    }

    /**
     * Hashes a 3D coordinate with a seed, producing a well-distributed long.
     */
    public static long hash3D(long seed, int x, int y, int z) {
        long h = seed + PRIME5;
        h += 24;

        long k1 = (long) x * PRIME2;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= PRIME1;
        h ^= k1;
        h = Long.rotateLeft(h, 27) * PRIME1 + PRIME4;

        long k2 = (long) y * PRIME2;
        k2 = Long.rotateLeft(k2, 31);
        k2 *= PRIME1;
        h ^= k2;
        h = Long.rotateLeft(h, 27) * PRIME1 + PRIME4;

        long k3 = (long) z * PRIME2;
        k3 = Long.rotateLeft(k3, 31);
        k3 *= PRIME1;
        h ^= k3;
        h = Long.rotateLeft(h, 27) * PRIME1 + PRIME4;

        return avalanche(h);
    }

    /**
     * Converts a hash value to a double in the range [0, 1).
     */
    public static double hashToDouble(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    private static long avalanche(long h) {
        h ^= h >>> 33;
        h *= PRIME2;
        h ^= h >>> 29;
        h *= PRIME3;
        h ^= h >>> 32;
        return h;
    }
}
