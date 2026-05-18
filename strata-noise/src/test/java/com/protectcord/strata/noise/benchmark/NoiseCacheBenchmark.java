package com.protectcord.strata.noise.benchmark;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.NoiseFactory;
import com.protectcord.strata.noise.cache.NoiseCache;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Benchmark measuring cache hit rates and speedup from {@link NoiseCache}.
 *
 * <p>Simulates the access pattern of a 16x16 chunk area where many
 * pipeline stages sample the same coordinates repeatedly. The cache
 * should provide significant speedup when the working set fits within
 * the configured maximum size.</p>
 *
 * <p>Run with {@code --include-tags=benchmark}.</p>
 */
@Tag("benchmark")
class NoiseCacheBenchmark {

    private static final long SEED = 99999L;
    private static final int TOTAL_SAMPLES = 100_000;
    private static final int WARMUP_SAMPLES = 10_000;

    /**
     * Measures cached vs uncached throughput for a chunk-like access pattern
     * where many coordinates repeat (simulating multiple pipeline stages
     * querying the same positions).
     */
    @ParameterizedTest(name = "cacheSize={0}")
    @ValueSource(ints = {256, 1024, 4096, 16384})
    void measureCacheSpeedup(int cacheSize) {
        NamespacedKey key = NamespacedKey.strata("bench_cache");
        NoiseFunction base = NoiseFactory.create(key, NoiseType.SIMPLEX, SEED);

        // Pre-compute the coordinate sequence: 16x16 chunk area with repetition.
        // Each coordinate is sampled multiple times (simulating multiple passes
        // over the same terrain, as occurs during density + surface + biome queries).
        double[] xs = new double[TOTAL_SAMPLES];
        double[] zs = new double[TOTAL_SAMPLES];
        fillChunkCoordinates(xs, zs, TOTAL_SAMPLES);

        // --- Warmup both paths ---
        NoiseCache warmupCache = NoiseFactory.cached(base, cacheSize);
        warmup(warmupCache, xs, zs, WARMUP_SAMPLES);
        warmup(base, xs, zs, WARMUP_SAMPLES);

        // --- Measure uncached ---
        long uncachedStart = System.nanoTime();
        double uncachedSink = 0.0;
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            uncachedSink += base.sample(xs[i], zs[i]);
        }
        long uncachedElapsed = System.nanoTime() - uncachedStart;

        // --- Measure cached ---
        NoiseCache cached = NoiseFactory.cached(base, cacheSize);
        long cachedStart = System.nanoTime();
        double cachedSink = 0.0;
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            cachedSink += cached.sample(xs[i], zs[i]);
        }
        long cachedElapsed = System.nanoTime() - cachedStart;

        preventElimination(uncachedSink, cachedSink);

        double uncachedMs = uncachedElapsed / 1_000_000.0;
        double cachedMs = cachedElapsed / 1_000_000.0;
        double speedup = (double) uncachedElapsed / cachedElapsed;

        System.out.printf("Cache size=%-5d  uncached: %.2f ms  cached: %.2f ms  speedup: %.2fx%n",
                cacheSize, uncachedMs, cachedMs, speedup);
    }

    /**
     * Detailed test for the default cache size (4096), reporting per-sample costs.
     */
    @Test
    void measureDefaultCacheDetailed() {
        int cacheSize = 4096;
        NamespacedKey key = NamespacedKey.strata("bench_cache_detail");
        NoiseFunction base = NoiseFactory.create(key, NoiseType.SIMPLEX, SEED);

        double[] xs = new double[TOTAL_SAMPLES];
        double[] zs = new double[TOTAL_SAMPLES];
        fillChunkCoordinates(xs, zs, TOTAL_SAMPLES);

        // Warmup
        NoiseCache warmupCache = NoiseFactory.cached(base, cacheSize);
        warmup(warmupCache, xs, zs, WARMUP_SAMPLES);
        warmup(base, xs, zs, WARMUP_SAMPLES);

        // Uncached
        long uncachedStart = System.nanoTime();
        double uncachedSink = 0.0;
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            uncachedSink += base.sample(xs[i], zs[i]);
        }
        long uncachedElapsed = System.nanoTime() - uncachedStart;

        // Cached - first pass (cold cache, mostly misses)
        NoiseCache cached = NoiseFactory.cached(base, cacheSize);
        long coldStart = System.nanoTime();
        double coldSink = 0.0;
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            coldSink += cached.sample(xs[i], zs[i]);
        }
        long coldElapsed = System.nanoTime() - coldStart;

        // Cached - second pass (warm cache, mostly hits)
        long warmStart = System.nanoTime();
        double warmSink = 0.0;
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            warmSink += cached.sample(xs[i], zs[i]);
        }
        long warmElapsed = System.nanoTime() - warmStart;

        preventElimination(uncachedSink, coldSink, warmSink);

        double uncachedNs = (double) uncachedElapsed / TOTAL_SAMPLES;
        double coldNs = (double) coldElapsed / TOTAL_SAMPLES;
        double warmNs = (double) warmElapsed / TOTAL_SAMPLES;

        System.out.printf("Cache detail (size=%d, samples=%d):%n", cacheSize, TOTAL_SAMPLES);
        System.out.printf("  Uncached:        %.1f ns/sample%n", uncachedNs);
        System.out.printf("  Cached (cold):   %.1f ns/sample  (%.2fx vs uncached)%n",
                coldNs, (double) uncachedElapsed / coldElapsed);
        System.out.printf("  Cached (warm):   %.1f ns/sample  (%.2fx vs uncached)%n",
                warmNs, (double) uncachedElapsed / warmElapsed);
    }

    /**
     * Fills coordinate arrays with a chunk-like pattern where positions
     * repeat to simulate multiple pipeline stages sampling the same terrain.
     * The 16x16 grid of unique positions cycles through multiple "passes"
     * to produce the requested total number of samples.
     */
    private void fillChunkCoordinates(double[] xs, double[] zs, int total) {
        int uniquePositions = 16 * 16; // one chunk surface
        int idx = 0;
        while (idx < total) {
            for (int cx = 0; cx < 16 && idx < total; cx++) {
                for (int cz = 0; cz < 16 && idx < total; cz++) {
                    xs[idx] = cx;
                    zs[idx] = cz;
                    idx++;
                }
            }
        }
    }

    private void warmup(NoiseFunction noise, double[] xs, double[] zs, int samples) {
        double sink = 0.0;
        int limit = Math.min(samples, xs.length);
        for (int i = 0; i < limit; i++) {
            sink += noise.sample(xs[i], zs[i]);
        }
        preventElimination(sink);
    }

    @SuppressWarnings("unused")
    private static volatile double blackhole;

    private static void preventElimination(double... values) {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        blackhole = sum;
    }
}
