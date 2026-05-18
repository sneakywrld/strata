package com.protectcord.strata.noise.fractal;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.NoiseFactory;
import com.protectcord.strata.noise.algorithm.SimplexNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FractalNoise Tests")
class FractalNoiseTest {

    private static final long SEED = 12345L;
    private static final NamespacedKey KEY = NamespacedKey.strata("test_noise");

    @Test
    @DisplayName("2D output stays within [-1, 1] with default settings")
    void sample2dRangeWithDefaultSettings() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalNoise fractal = new FractalNoise(KEY, base, FractalSettings.defaultSettings());
        Random rng = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 200.0;
            double z = (rng.nextDouble() - 0.5) * 200.0;
            double value = fractal.sample(x, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    "2D fractal out of range: " + value + " at (" + x + ", " + z + ")");
        }
    }

    @Test
    @DisplayName("3D output stays within [-1, 1] with default settings")
    void sample3dRangeWithDefaultSettings() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalNoise fractal = new FractalNoise(KEY, base, FractalSettings.defaultSettings());
        Random rng = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 200.0;
            double y = (rng.nextDouble() - 0.5) * 200.0;
            double z = (rng.nextDouble() - 0.5) * 200.0;
            double value = fractal.sample(x, y, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    "3D fractal out of range: " + value);
        }
    }

    @Test
    @DisplayName("Output stays within [-1, 1] with high octave count")
    void rangeWithHighOctaves() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalSettings settings = new FractalSettings(8, 1.0, 1.0, 2.0, 0.5);
        FractalNoise fractal = new FractalNoise(KEY, base, settings);
        Random rng = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 200.0;
            double z = (rng.nextDouble() - 0.5) * 200.0;
            double value = fractal.sample(x, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    "High-octave fractal out of range: " + value);
        }
    }

    @Test
    @DisplayName("More octaves produce different output than single octave")
    void moreOctavesAddDetail() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalSettings singleOctave = new FractalSettings(1, 1.0, 1.0, 2.0, 0.5);
        FractalSettings fourOctaves = FractalSettings.defaultSettings();

        FractalNoise single = new FractalNoise(KEY, base, singleOctave);
        FractalNoise multi = new FractalNoise(KEY, base, fourOctaves);

        int differences = 0;
        for (int i = 0; i < 200; i++) {
            double x = i * 0.37;
            double z = i * 0.73;
            if (single.sample(x, z) != multi.sample(x, z)) {
                differences++;
            }
        }
        assertTrue(differences > 100,
                "4-octave output should differ from 1-octave at most coords, got " + differences + "/200");
    }

    @Test
    @DisplayName("2D output is deterministic")
    void sample2dIsDeterministic() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalNoise fractal = new FractalNoise(KEY, base, FractalSettings.defaultSettings());
        for (int i = 0; i < 100; i++) {
            double x = i * 0.73;
            double z = i * 1.37;
            double first = fractal.sample(x, z);
            double second = fractal.sample(x, z);
            assertEquals(first, second, 0.0);
        }
    }

    @Test
    @DisplayName("3D output is deterministic")
    void sample3dIsDeterministic() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalNoise fractal = new FractalNoise(KEY, base, FractalSettings.defaultSettings());
        for (int i = 0; i < 100; i++) {
            double x = i * 0.73;
            double y = i * 0.41;
            double z = i * 1.37;
            double first = fractal.sample(x, y, z);
            double second = fractal.sample(x, y, z);
            assertEquals(first, second, 0.0);
        }
    }

    @Test
    @DisplayName("NoiseFactory.fractal() produces a working FractalNoise")
    void factoryFractalProducesFractalNoise() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseFunction fractal = NoiseFactory.fractal(KEY, base, FractalSettings.defaultSettings());
        assertNotNull(fractal);
        assertInstanceOf(FractalNoise.class, fractal);

        double v1 = fractal.sample(1.0, 2.0);
        double v2 = fractal.sample(1.0, 2.0);
        assertEquals(v1, v2, 0.0);
    }

    @Test
    @DisplayName("Two FractalNoise instances with the same base and settings produce identical output")
    void reconstructedFractalIsDeterministic() {
        NoiseFunction baseA = new SimplexNoise(KEY, SEED);
        NoiseFunction baseB = new SimplexNoise(KEY, SEED);
        FractalNoise fractalA = new FractalNoise(KEY, baseA, FractalSettings.defaultSettings());
        FractalNoise fractalB = new FractalNoise(KEY, baseB, FractalSettings.defaultSettings());

        for (int i = 0; i < 50; i++) {
            double x = i * 1.23;
            double z = i * 4.56;
            assertEquals(fractalA.sample(x, z), fractalB.sample(x, z), 0.0);
            assertEquals(fractalA.sample(x, z, x), fractalB.sample(x, z, x), 0.0);
        }
    }

    @Test
    @DisplayName("Single octave fractal matches base noise output")
    void singleOctaveMatchesBase() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalSettings single = new FractalSettings(1, 1.0, 1.0, 2.0, 0.5);
        FractalNoise fractal = new FractalNoise(KEY, base, single);

        for (int i = 0; i < 50; i++) {
            double x = i * 1.1;
            double z = i * 2.3;
            assertEquals(base.sample(x, z), fractal.sample(x, z), 1e-12,
                    "Single octave fractal should match base noise");
        }
    }

    @Test
    @DisplayName("key() returns the assigned key")
    void keyReturnsAssignedKey() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalNoise fractal = new FractalNoise(KEY, base, FractalSettings.defaultSettings());
        assertEquals(KEY, fractal.key());
    }

    @Test
    @DisplayName("Different frequencies produce different outputs")
    void differentFrequenciesProduceDifferentOutput() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        FractalSettings low = new FractalSettings(4, 0.5, 1.0, 2.0, 0.5);
        FractalSettings high = new FractalSettings(4, 4.0, 1.0, 2.0, 0.5);

        FractalNoise fractalLow = new FractalNoise(KEY, base, low);
        FractalNoise fractalHigh = new FractalNoise(KEY, base, high);

        int differences = 0;
        for (int i = 0; i < 100; i++) {
            double x = i * 0.37;
            double z = i * 0.73;
            if (fractalLow.sample(x, z) != fractalHigh.sample(x, z)) {
                differences++;
            }
        }
        assertTrue(differences > 50,
                "Different frequencies should produce different outputs, got " + differences + "/100");
    }
}
