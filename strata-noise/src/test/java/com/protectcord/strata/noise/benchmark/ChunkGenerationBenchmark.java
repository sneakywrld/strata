package com.protectcord.strata.noise.benchmark;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.NoiseFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Simulates the noise workload of generating a full Minecraft chunk.
 *
 * <p>Models a realistic chunk generation pipeline consisting of:</p>
 * <ul>
 *   <li><b>3D density noise</b> -- sampled every 4th block, producing
 *       4 x 4 x 96 = 1,536 samples per chunk (16 x 16 x 384 volume,
 *       sub-sampled at 4-block intervals for later interpolation).</li>
 *   <li><b>2D climate noises</b> -- 5 climate parameters (temperature,
 *       humidity, continentalness, erosion, weirdness) each sampled across
 *       the 16 x 16 surface = 1,280 samples per chunk.</li>
 *   <li><b>3D cave noises</b> -- cheese, spaghetti, and noodle noise
 *       sampled at the same sub-sampled 3D grid = 4,608 samples per chunk.</li>
 * </ul>
 *
 * <p>Total: ~7,424 noise samples per chunk. This benchmark generates
 * 100 chunks and reports the average time per chunk and the implied
 * throughput in chunks/sec.</p>
 *
 * <p>Run with {@code --include-tags=benchmark}.</p>
 */
@Tag("benchmark")
class ChunkGenerationBenchmark {

    private static final long SEED = 20240101L;
    private static final int CHUNK_COUNT = 100;
    private static final int WARMUP_CHUNKS = 10;

    // Chunk dimensions
    private static final int CHUNK_X = 16;
    private static final int CHUNK_Z = 16;
    private static final int CHUNK_Y = 384;
    private static final int SAMPLE_STEP = 4;

    // Derived 3D grid dimensions (sub-sampled every SAMPLE_STEP blocks)
    private static final int GRID_X = CHUNK_X / SAMPLE_STEP;   // 4
    private static final int GRID_Z = CHUNK_Z / SAMPLE_STEP;   // 4
    private static final int GRID_Y = CHUNK_Y / SAMPLE_STEP;   // 96

    @Test
    void measureChunkGenerationThroughput() {
        // --- Create noise functions for the simulated pipeline ---

        // 3D density noise (fractal simplex, 4 octaves)
        NoiseFunction densityBase = NoiseFactory.create(
                NamespacedKey.strata("bench_density_base"), NoiseType.SIMPLEX, SEED);
        NoiseFunction density = NoiseFactory.fractal(
                NamespacedKey.strata("bench_density"),
                densityBase,
                FractalSettings.defaultSettings());

        // 2D climate noises (5 parameters, each fractal with 4 octaves)
        NoiseFunction[] climate = new NoiseFunction[5];
        String[] climateNames = {"temperature", "humidity", "continentalness", "erosion", "weirdness"};
        for (int i = 0; i < climate.length; i++) {
            NoiseFunction base = NoiseFactory.create(
                    NamespacedKey.strata("bench_" + climateNames[i] + "_base"),
                    NoiseType.PERLIN,
                    SEED + i + 1);
            climate[i] = NoiseFactory.fractal(
                    NamespacedKey.strata("bench_" + climateNames[i]),
                    base,
                    new FractalSettings(4, 0.005, 1.0, 2.0, 0.5));
        }

        // 3D cave noises (cheese, spaghetti, noodle)
        NoiseFunction cheeseBase = NoiseFactory.create(
                NamespacedKey.strata("bench_cheese_base"), NoiseType.PERLIN, SEED + 100);
        NoiseFunction cheese = NoiseFactory.fractal(
                NamespacedKey.strata("bench_cheese"),
                cheeseBase,
                new FractalSettings(3, 0.02, 1.0, 2.0, 0.5));

        NoiseFunction spaghettiBase = NoiseFactory.create(
                NamespacedKey.strata("bench_spaghetti_base"), NoiseType.OPEN_SIMPLEX_2, SEED + 200);
        NoiseFunction spaghetti = NoiseFactory.fractal(
                NamespacedKey.strata("bench_spaghetti"),
                spaghettiBase,
                new FractalSettings(3, 0.03, 1.0, 2.0, 0.5));

        NoiseFunction noodleBase = NoiseFactory.create(
                NamespacedKey.strata("bench_noodle_base"), NoiseType.SIMPLEX, SEED + 300);
        NoiseFunction noodle = NoiseFactory.fractal(
                NamespacedKey.strata("bench_noodle"),
                noodleBase,
                new FractalSettings(2, 0.04, 1.0, 2.0, 0.5));

        // --- Warmup ---
        for (int c = 0; c < WARMUP_CHUNKS; c++) {
            generateChunk(c * CHUNK_X, c * CHUNK_Z, density, climate, cheese, spaghetti, noodle);
        }

        // --- Measured run ---
        long totalNanos = 0;
        long minNanos = Long.MAX_VALUE;
        long maxNanos = Long.MIN_VALUE;

        for (int c = 0; c < CHUNK_COUNT; c++) {
            int chunkOriginX = c * CHUNK_X;
            int chunkOriginZ = (c / 10) * CHUNK_Z;

            long start = System.nanoTime();
            double sink = generateChunk(chunkOriginX, chunkOriginZ, density, climate, cheese, spaghetti, noodle);
            long elapsed = System.nanoTime() - start;

            preventElimination(sink);

            totalNanos += elapsed;
            minNanos = Math.min(minNanos, elapsed);
            maxNanos = Math.max(maxNanos, elapsed);
        }

        double avgMs = (totalNanos / (double) CHUNK_COUNT) / 1_000_000.0;
        double minMs = minNanos / 1_000_000.0;
        double maxMs = maxNanos / 1_000_000.0;
        int chunksPerSec = (int) (1000.0 / avgMs);

        int samplesPerChunk = (GRID_X * GRID_Z * GRID_Y)          // density: 1,536
                + (5 * CHUNK_X * CHUNK_Z)                          // climate: 1,280
                + (3 * GRID_X * GRID_Z * GRID_Y);                 // caves:   4,608
        double totalSamplesPerSec = (double) samplesPerChunk * chunksPerSec;

        System.out.println("=== Chunk Generation Benchmark ===");
        System.out.printf("Chunks generated: %d%n", CHUNK_COUNT);
        System.out.printf("Samples per chunk: %,d (density=%d, climate=%d, cave=%d)%n",
                samplesPerChunk,
                GRID_X * GRID_Z * GRID_Y,
                5 * CHUNK_X * CHUNK_Z,
                3 * GRID_X * GRID_Z * GRID_Y);
        System.out.printf("Average chunk generation: %.2f ms (%d chunks/sec)%n", avgMs, chunksPerSec);
        System.out.printf("Min: %.2f ms  Max: %.2f ms%n", minMs, maxMs);
        System.out.printf("Effective throughput: %.2f M noise samples/sec%n", totalSamplesPerSec / 1_000_000.0);
    }

    /**
     * Simulates generating one chunk's worth of noise data, returning the
     * accumulated sum of all samples (used to prevent dead-code elimination).
     */
    private double generateChunk(
            int originX, int originZ,
            NoiseFunction density,
            NoiseFunction[] climate,
            NoiseFunction cheese, NoiseFunction spaghetti, NoiseFunction noodle) {

        double sink = 0.0;

        // --- 3D density noise (sub-sampled every 4 blocks) ---
        for (int gx = 0; gx < GRID_X; gx++) {
            double wx = originX + gx * SAMPLE_STEP;
            for (int gz = 0; gz < GRID_Z; gz++) {
                double wz = originZ + gz * SAMPLE_STEP;
                for (int gy = 0; gy < GRID_Y; gy++) {
                    double wy = -64 + gy * SAMPLE_STEP; // world Y starts at -64
                    sink += density.sample(wx, wy, wz);
                }
            }
        }

        // --- 2D climate noises at chunk surface ---
        for (int cx = 0; cx < CHUNK_X; cx++) {
            double wx = originX + cx;
            for (int cz = 0; cz < CHUNK_Z; cz++) {
                double wz = originZ + cz;
                for (NoiseFunction c : climate) {
                    sink += c.sample(wx, wz);
                }
            }
        }

        // --- 3D cave noises (cheese, spaghetti, noodle at sub-sampled grid) ---
        for (int gx = 0; gx < GRID_X; gx++) {
            double wx = originX + gx * SAMPLE_STEP;
            for (int gz = 0; gz < GRID_Z; gz++) {
                double wz = originZ + gz * SAMPLE_STEP;
                for (int gy = 0; gy < GRID_Y; gy++) {
                    double wy = -64 + gy * SAMPLE_STEP;
                    sink += cheese.sample(wx, wy, wz);
                    sink += spaghetti.sample(wx, wy, wz);
                    sink += noodle.sample(wx, wy, wz);
                }
            }
        }

        return sink;
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
