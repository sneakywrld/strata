package com.protectcord.strata.noise.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;

/**
 * Caching wrapper for noise functions.
 * Uses Caffeine for an LRU cache of recently sampled positions,
 * significantly speeding up repeated queries at the same coordinates
 * (common during multi-stage pipeline access).
 */
public final class NoiseCache implements NoiseFunction {

    private final NoiseFunction delegate;
    private final Cache<Long, Double> cache2D;
    private final Cache<Long, Double> cache3D;

    public NoiseCache(NoiseFunction delegate, int maxSize) {
        this.delegate = delegate;
        this.cache2D = Caffeine.newBuilder().maximumSize(maxSize).build();
        this.cache3D = Caffeine.newBuilder().maximumSize(maxSize).build();
    }

    public NoiseCache(NoiseFunction delegate) {
        this(delegate, 4096);
    }

    @Override
    public NamespacedKey key() {
        return delegate.key();
    }

    @Override
    public double sample(double x, double z) {
        long packedKey = packKey2D(x, z);
        Double cached = cache2D.getIfPresent(packedKey);
        if (cached != null) return cached;
        double value = delegate.sample(x, z);
        cache2D.put(packedKey, value);
        return value;
    }

    @Override
    public double sample(double x, double y, double z) {
        long packedKey = packKey3D(x, y, z);
        Double cached = cache3D.getIfPresent(packedKey);
        if (cached != null) return cached;
        double value = delegate.sample(x, y, z);
        cache3D.put(packedKey, value);
        return value;
    }

    @Override
    public double minValue() { return delegate.minValue(); }

    @Override
    public double maxValue() { return delegate.maxValue(); }

    /**
     * Clears all cached values. Call after seed/config changes.
     */
    public void invalidate() {
        cache2D.invalidateAll();
        cache3D.invalidateAll();
    }

    private static long packKey2D(double x, double z) {
        int xi = (int) (x * 16);
        int zi = (int) (z * 16);
        return ((long) xi << 32) | (zi & 0xFFFFFFFFL);
    }

    private static long packKey3D(double x, double y, double z) {
        int xi = (int) (x * 16);
        int yi = (int) (y * 16);
        int zi = (int) (z * 16);
        return ((long) xi << 42) | ((long) (yi & 0x1FFFFF) << 21) | (zi & 0x1FFFFF);
    }
}
