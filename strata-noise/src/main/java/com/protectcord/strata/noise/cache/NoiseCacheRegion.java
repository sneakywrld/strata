package com.protectcord.strata.noise.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.function.Supplier;

public final class NoiseCacheRegion {

    private record CacheKey(String noiseId, int chunkX, int chunkZ) {}

    private final Cache<CacheKey, double[]> cache;

    public NoiseCacheRegion(int maxChunks) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxChunks)
                .recordStats()
                .build();
    }

    public NoiseCacheRegion() {
        this(1024);
    }

    public double[] getOrCompute(String noiseId, int chunkX, int chunkZ, Supplier<double[]> supplier) {
        CacheKey key = new CacheKey(noiseId, chunkX, chunkZ);
        double[] result = cache.getIfPresent(key);
        if (result != null) {
            return result;
        }
        result = supplier.get();
        cache.put(key, result);
        return result;
    }

    public void invalidateChunk(int chunkX, int chunkZ) {
        cache.asMap().keySet().removeIf(k -> k.chunkX() == chunkX && k.chunkZ() == chunkZ);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public long size() {
        return cache.estimatedSize();
    }
}
