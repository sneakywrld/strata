package com.protectcord.strata.noise.benchmark;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.NoiseFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Throughput benchmark for each noise algorithm.
 *
 * <p>Measures how many 2D and 3D samples per second each {@link NoiseType}
 * can produce. WHITE and CONSTANT are excluded because their implementations
 * are trivially fast and not representative of real workload.</p>
 *
 * <p>Run with {@code --include-tags=benchmark} to execute these tests
 * separately from the normal test suite.</p>
 */
@Tag("benchmark")
class NoiseThroughputBenchmark {

    private static final long SEED = 12345L;
    private static final int WARMUP_SAMPLES = 100_000;
    private static final int MEASURED_SAMPLES = 1_000_000;

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = NoiseType.class, names = {"WHITE", "CONSTANT", "OPEN_SIMPLEX_2S"}, mode = EnumSource.Mode.EXCLUDE)
    void measureThroughput(NoiseType type) {
        NamespacedKey key = NamespacedKey.strata("bench_" + type.name().toLowerCase());
        NoiseFunction noise = NoiseFactory.create(key, type, SEED);

        // --- Warmup 2D ---
        warmup2D(noise, WARMUP_SAMPLES);

        // --- Measure 2D ---
        int gridSize2D = (int) Math.ceil(Math.sqrt(MEASURED_SAMPLES));
        double spacing = 0.01;

        long start2D = System.nanoTime();
        double sink2D = 0.0;
        int count2D = 0;
        for (int ix = 0; ix < gridSize2D && count2D < MEASURED_SAMPLES; ix++) {
            double x = ix * spacing;
            for (int iz = 0; iz < gridSize2D && count2D < MEASURED_SAMPLES; iz++) {
                double z = iz * spacing;
                sink2D += noise.sample(x, z);
                count2D++;
            }
        }
        long elapsed2D = System.nanoTime() - start2D;

        // --- Warmup 3D ---
        warmup3D(noise, WARMUP_SAMPLES);

        // --- Measure 3D ---
        int gridSize3D = (int) Math.ceil(Math.cbrt(MEASURED_SAMPLES));
        long start3D = System.nanoTime();
        double sink3D = 0.0;
        int count3D = 0;
        for (int ix = 0; ix < gridSize3D && count3D < MEASURED_SAMPLES; ix++) {
            double x = ix * spacing;
            for (int iy = 0; iy < gridSize3D && count3D < MEASURED_SAMPLES; iy++) {
                double y = iy * spacing;
                for (int iz = 0; iz < gridSize3D && count3D < MEASURED_SAMPLES; iz++) {
                    double z = iz * spacing;
                    sink3D += noise.sample(x, y, z);
                    count3D++;
                }
            }
        }
        long elapsed3D = System.nanoTime() - start3D;

        // Prevent dead-code elimination
        preventElimination(sink2D, sink3D);

        double rate2D = (count2D / 1_000_000.0) / (elapsed2D / 1_000_000_000.0);
        double rate3D = (count3D / 1_000_000.0) / (elapsed3D / 1_000_000_000.0);

        System.out.printf("%-20s  2D: %.2f M samples/sec  3D: %.2f M samples/sec%n", type, rate2D, rate3D);
    }

    private void warmup2D(NoiseFunction noise, int samples) {
        int gridSize = (int) Math.ceil(Math.sqrt(samples));
        double spacing = 0.01;
        double sink = 0.0;
        int count = 0;
        for (int ix = 0; ix < gridSize && count < samples; ix++) {
            for (int iz = 0; iz < gridSize && count < samples; iz++) {
                sink += noise.sample(ix * spacing, iz * spacing);
                count++;
            }
        }
        preventElimination(sink);
    }

    private void warmup3D(NoiseFunction noise, int samples) {
        int gridSize = (int) Math.ceil(Math.cbrt(samples));
        double spacing = 0.01;
        double sink = 0.0;
        int count = 0;
        for (int ix = 0; ix < gridSize && count < samples; ix++) {
            for (int iy = 0; iy < gridSize && count < samples; iy++) {
                for (int iz = 0; iz < gridSize && count < samples; iz++) {
                    sink += noise.sample(ix * spacing, iy * spacing, iz * spacing);
                    count++;
                }
            }
        }
        preventElimination(sink);
    }

    /**
     * Consumes values to prevent JIT dead-code elimination of the sampling loops.
     * The volatile write forces the JIT to keep all upstream computation alive.
     */
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
