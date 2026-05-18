package com.protectcord.strata.noise.benchmark;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.NoiseFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Benchmark demonstrating how fractal noise throughput degrades as the
 * number of stacked octaves increases.
 *
 * <p>Each octave requires a full evaluation of the underlying noise
 * function, so throughput is expected to drop roughly linearly with
 * octave count. This benchmark quantifies the actual cost so that
 * world-gen profiles can make informed trade-offs between quality
 * and performance.</p>
 *
 * <p>Run with {@code --include-tags=benchmark}.</p>
 */
@Tag("benchmark")
class FractalNoiseBenchmark {

    private static final long SEED = 54321L;
    private static final int WARMUP_SAMPLES = 50_000;
    private static final int MEASURED_SAMPLES = 500_000;
    private static final double SPACING = 0.01;

    @ParameterizedTest(name = "octaves={0}")
    @ValueSource(ints = {1, 2, 4, 8, 16})
    void measureFractalThroughput(int octaves) {
        NamespacedKey baseKey = NamespacedKey.strata("bench_simplex_base");
        NamespacedKey fractalKey = NamespacedKey.strata("bench_fractal_" + octaves);

        NoiseFunction base = NoiseFactory.create(baseKey, NoiseType.SIMPLEX, SEED);
        FractalSettings settings = new FractalSettings(octaves, 1.0, 1.0, 2.0, 0.5);
        NoiseFunction fractal = NoiseFactory.fractal(fractalKey, base, settings);

        // --- Warmup ---
        warmup(fractal, WARMUP_SAMPLES);

        // --- Measure ---
        int gridSize = (int) Math.ceil(Math.sqrt(MEASURED_SAMPLES));

        long start = System.nanoTime();
        double sink = 0.0;
        int count = 0;
        for (int ix = 0; ix < gridSize && count < MEASURED_SAMPLES; ix++) {
            double x = ix * SPACING;
            for (int iz = 0; iz < gridSize && count < MEASURED_SAMPLES; iz++) {
                double z = iz * SPACING;
                sink += fractal.sample(x, z);
                count++;
            }
        }
        long elapsed = System.nanoTime() - start;

        preventElimination(sink);

        double rateMsps = (count / 1_000_000.0) / (elapsed / 1_000_000_000.0);
        double avgNsPerSample = (double) elapsed / count;

        System.out.printf("Fractal octaves=%-2d  %.2f M samples/sec  (%.1f ns/sample)%n",
                octaves, rateMsps, avgNsPerSample);
    }

    private void warmup(NoiseFunction noise, int samples) {
        int gridSize = (int) Math.ceil(Math.sqrt(samples));
        double sink = 0.0;
        int count = 0;
        for (int ix = 0; ix < gridSize && count < samples; ix++) {
            for (int iz = 0; iz < gridSize && count < samples; iz++) {
                sink += noise.sample(ix * SPACING, iz * SPACING);
                count++;
            }
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
