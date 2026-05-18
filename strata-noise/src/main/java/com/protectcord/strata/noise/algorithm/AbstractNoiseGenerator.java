package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;

/**
 * Base class for noise algorithm implementations.
 */
public abstract class AbstractNoiseGenerator implements NoiseFunction {

    protected final NamespacedKey key;
    protected final long seed;

    protected AbstractNoiseGenerator(NamespacedKey key, long seed) {
        this.key = key;
        this.seed = seed;
    }

    @Override
    public NamespacedKey key() {
        return key;
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    /**
     * Creates a permutation table seeded from the given seed.
     */
    protected static int[] createPermutationTable(long seed) {
        int[] perm = new int[512];
        int[] base = new int[256];
        for (int i = 0; i < 256; i++) base[i] = i;

        // Fisher-Yates shuffle
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = base[i];
            base[i] = base[j];
            base[j] = tmp;
        }

        // Double to avoid wrapping
        System.arraycopy(base, 0, perm, 0, 256);
        System.arraycopy(base, 0, perm, 256, 256);
        return perm;
    }
}
