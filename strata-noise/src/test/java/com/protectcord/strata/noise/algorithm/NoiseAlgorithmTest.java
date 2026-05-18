package com.protectcord.strata.noise.algorithm;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.NoiseFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Noise Algorithm Tests")
class NoiseAlgorithmTest {

    private static final long SEED = 12345L;
    private static final long ALT_SEED = 99999L;
    private static final NamespacedKey KEY = NamespacedKey.strata("test_noise");

    static Stream<Arguments> allNoiseAlgorithms() {
        return Stream.of(
                Arguments.of("SimplexNoise", new SimplexNoise(KEY, SEED)),
                Arguments.of("PerlinNoise", new PerlinNoise(KEY, SEED)),
                Arguments.of("OpenSimplex2Noise", new OpenSimplex2Noise(KEY, SEED)),
                Arguments.of("CellularNoise", new CellularNoise(KEY, SEED)),
                Arguments.of("ValueNoise", new ValueNoise(KEY, SEED)),
                Arguments.of("RidgedMultifractalNoise", new RidgedMultifractalNoise(KEY, SEED, FractalSettings.defaultSettings())),
                Arguments.of("WhiteNoise", new WhiteNoise(KEY, SEED))
        );
    }

    static Stream<Arguments> pairedNoiseAlgorithms() {
        return Stream.of(
                Arguments.of("SimplexNoise",
                        new SimplexNoise(KEY, SEED), new SimplexNoise(KEY, ALT_SEED)),
                Arguments.of("PerlinNoise",
                        new PerlinNoise(KEY, SEED), new PerlinNoise(KEY, ALT_SEED)),
                Arguments.of("OpenSimplex2Noise",
                        new OpenSimplex2Noise(KEY, SEED), new OpenSimplex2Noise(KEY, ALT_SEED)),
                Arguments.of("CellularNoise",
                        new CellularNoise(KEY, SEED), new CellularNoise(KEY, ALT_SEED)),
                Arguments.of("ValueNoise",
                        new ValueNoise(KEY, SEED), new ValueNoise(KEY, ALT_SEED)),
                Arguments.of("RidgedMultifractalNoise",
                        new RidgedMultifractalNoise(KEY, SEED, FractalSettings.defaultSettings()),
                        new RidgedMultifractalNoise(KEY, ALT_SEED, FractalSettings.defaultSettings())),
                Arguments.of("WhiteNoise",
                        new WhiteNoise(KEY, SEED), new WhiteNoise(KEY, ALT_SEED))
        );
    }

    @ParameterizedTest(name = "{0}: 2D determinism")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("Same seed and coordinates produce identical 2D output")
    void sample2dIsDeterministic(String name, NoiseFunction noise) {
        for (int i = 0; i < 100; i++) {
            double x = i * 0.73;
            double z = i * 1.37;
            double first = noise.sample(x, z);
            double second = noise.sample(x, z);
            assertEquals(first, second, 0.0,
                    name + " 2D not deterministic at (" + x + ", " + z + ")");
        }
    }

    @ParameterizedTest(name = "{0}: 3D determinism")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("Same seed and coordinates produce identical 3D output")
    void sample3dIsDeterministic(String name, NoiseFunction noise) {
        for (int i = 0; i < 100; i++) {
            double x = i * 0.73;
            double y = i * 0.41;
            double z = i * 1.37;
            double first = noise.sample(x, y, z);
            double second = noise.sample(x, y, z);
            assertEquals(first, second, 0.0,
                    name + " 3D not deterministic at (" + x + ", " + y + ", " + z + ")");
        }
    }

    static Stream<Arguments> strictRangeAlgorithms() {
        return Stream.of(
                Arguments.of("SimplexNoise", new SimplexNoise(KEY, SEED)),
                Arguments.of("PerlinNoise", new PerlinNoise(KEY, SEED)),
                Arguments.of("OpenSimplex2Noise", new OpenSimplex2Noise(KEY, SEED)),
                Arguments.of("ValueNoise", new ValueNoise(KEY, SEED)),
                Arguments.of("WhiteNoise", new WhiteNoise(KEY, SEED))
        );
    }

    @ParameterizedTest(name = "{0}: 2D range [-1, 1]")
    @MethodSource("strictRangeAlgorithms")
    @DisplayName("2D output stays within [-1, 1] over 10000 random samples")
    void sample2dRange(String name, NoiseFunction noise) {
        Random rng = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 200.0;
            double z = (rng.nextDouble() - 0.5) * 200.0;
            double value = noise.sample(x, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    name + " 2D out of range: " + value + " at (" + x + ", " + z + ")");
        }
    }

    @ParameterizedTest(name = "{0}: 3D range [-1, 1]")
    @MethodSource("strictRangeAlgorithms")
    @DisplayName("3D output stays within [-1, 1] over 10000 random samples")
    void sample3dRange(String name, NoiseFunction noise) {
        Random rng = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            double x = (rng.nextDouble() - 0.5) * 200.0;
            double y = (rng.nextDouble() - 0.5) * 200.0;
            double z = (rng.nextDouble() - 0.5) * 200.0;
            double value = noise.sample(x, y, z);
            assertTrue(value >= -1.0 && value <= 1.0,
                    name + " 3D out of range: " + value + " at (" + x + ", " + y + ", " + z + ")");
        }
    }

    @Test
    @DisplayName("CellularNoise and RidgedMultifractal produce finite bounded values")
    void wideRangeAlgorithmsProduceFiniteValues() {
        NoiseFunction[] wideRange = {
                new CellularNoise(KEY, SEED),
                new RidgedMultifractalNoise(KEY, SEED, FractalSettings.defaultSettings())
        };
        Random rng = new Random(42);
        for (NoiseFunction noise : wideRange) {
            for (int i = 0; i < 5_000; i++) {
                double x = (rng.nextDouble() - 0.5) * 200.0;
                double z = (rng.nextDouble() - 0.5) * 200.0;
                double v2 = noise.sample(x, z);
                double v3 = noise.sample(x, rng.nextDouble() * 200 - 100, z);
                assertTrue(Double.isFinite(v2), noise.key() + " 2D not finite: " + v2);
                assertTrue(Double.isFinite(v3), noise.key() + " 3D not finite: " + v3);
            }
        }
    }

    @ParameterizedTest(name = "{0}: seed sensitivity")
    @MethodSource("pairedNoiseAlgorithms")
    @DisplayName("Different seeds produce different output at the same coordinates")
    void differentSeedsProduceDifferentOutput(String name, NoiseFunction noiseA, NoiseFunction noiseB) {
        int differences = 0;
        for (int i = 0; i < 100; i++) {
            double x = i * 1.13;
            double z = i * 2.07;
            if (noiseA.sample(x, z) != noiseB.sample(x, z)) {
                differences++;
            }
        }
        assertTrue(differences > 50,
                name + " with different seeds only diverged " + differences + "/100 times");
    }

    @ParameterizedTest(name = "{0}: spatial variation")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("Output varies across different coordinates")
    void spatialVariation(String name, NoiseFunction noise) {
        double first = noise.sample(0.0, 0.0);
        int variations = 0;
        for (int i = 1; i <= 100; i++) {
            double x = i * 0.7;
            double z = i * 1.3;
            if (noise.sample(x, z) != first) {
                variations++;
            }
        }
        assertTrue(variations > 50,
                name + " shows too little spatial variation: only " + variations + "/100 differed");
    }

    @ParameterizedTest(name = "{0}: 3D spatial variation")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("3D output varies across different coordinates")
    void spatialVariation3d(String name, NoiseFunction noise) {
        double first = noise.sample(0.0, 0.0, 0.0);
        int variations = 0;
        for (int i = 1; i <= 100; i++) {
            double x = i * 0.7;
            double y = i * 0.3;
            double z = i * 1.3;
            if (noise.sample(x, y, z) != first) {
                variations++;
            }
        }
        assertTrue(variations > 50,
                name + " 3D shows too little spatial variation: only " + variations + "/100 differed");
    }

    @ParameterizedTest(name = "{0}: key() returns the assigned key")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("key() returns the NamespacedKey passed at construction")
    void keyReturnsAssignedKey(String name, NoiseFunction noise) {
        assertEquals(KEY, noise.key());
    }

    @ParameterizedTest(name = "{0}: minValue and maxValue are valid")
    @MethodSource("allNoiseAlgorithms")
    @DisplayName("minValue() <= maxValue()")
    void minMaxValueConsistent(String name, NoiseFunction noise) {
        assertTrue(noise.minValue() <= noise.maxValue(),
                name + " minValue " + noise.minValue() + " > maxValue " + noise.maxValue());
    }

    @Nested
    @DisplayName("CellularNoise ReturnType Variants")
    class CellularReturnTypeTests {

        @Test
        @DisplayName("DISTANCE return type produces finite values")
        void distanceReturnType() {
            CellularNoise noise = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.DISTANCE);
            Random rng = new Random(42);
            for (int i = 0; i < 5_000; i++) {
                double x = (rng.nextDouble() - 0.5) * 100.0;
                double z = (rng.nextDouble() - 0.5) * 100.0;
                double value = noise.sample(x, z);
                assertTrue(Double.isFinite(value), "DISTANCE 2D not finite: " + value);
                assertTrue(value >= -1.0 && value <= 2.0,
                        "DISTANCE 2D out of expected range: " + value);
            }
        }

        @Test
        @DisplayName("EDGE_DISTANCE return type produces finite values")
        void edgeDistanceReturnType() {
            CellularNoise noise = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.EDGE_DISTANCE);
            Random rng = new Random(42);
            for (int i = 0; i < 5_000; i++) {
                double x = (rng.nextDouble() - 0.5) * 100.0;
                double z = (rng.nextDouble() - 0.5) * 100.0;
                double value = noise.sample(x, z);
                assertTrue(Double.isFinite(value), "EDGE_DISTANCE 2D not finite: " + value);
                assertTrue(value >= -1.0 && value <= 2.0,
                        "EDGE_DISTANCE 2D out of expected range: " + value);
            }
        }

        @Test
        @DisplayName("CELL_VALUE return type produces values in [-1, 1]")
        void cellValueReturnType() {
            CellularNoise noise = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.CELL_VALUE);
            Random rng = new Random(42);
            for (int i = 0; i < 5_000; i++) {
                double x = (rng.nextDouble() - 0.5) * 100.0;
                double z = (rng.nextDouble() - 0.5) * 100.0;
                double value = noise.sample(x, z);
                assertTrue(value >= -1.0 && value <= 1.0,
                        "CELL_VALUE 2D out of range: " + value);
            }
        }

        @Test
        @DisplayName("Different return types produce different outputs")
        void differentReturnTypesProduceDifferentOutputs() {
            CellularNoise dist = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.DISTANCE);
            CellularNoise edge = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.EDGE_DISTANCE);
            CellularNoise cell = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.CELL_VALUE);

            int distVsEdge = 0;
            int distVsCell = 0;
            for (int i = 0; i < 100; i++) {
                double x = i * 0.7;
                double z = i * 1.3;
                if (dist.sample(x, z) != edge.sample(x, z)) distVsEdge++;
                if (dist.sample(x, z) != cell.sample(x, z)) distVsCell++;
            }
            assertTrue(distVsEdge > 50, "DISTANCE and EDGE_DISTANCE too similar");
            assertTrue(distVsCell > 50, "DISTANCE and CELL_VALUE too similar");
        }

        @Test
        @DisplayName("CELL_VALUE is constant within a cell")
        void cellValueIsConstantWithinCell() {
            CellularNoise noise = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.CELL_VALUE);
            double val1 = noise.sample(0.01, 0.01);
            double val2 = noise.sample(0.02, 0.02);
            assertEquals(val1, val2, 0.0,
                    "CELL_VALUE should be identical for nearby points in the same cell");
        }

        @Test
        @DisplayName("3D DISTANCE return type produces finite values")
        void distance3dReturnType() {
            CellularNoise noise = new CellularNoise(KEY, SEED, CellularNoise.ReturnType.DISTANCE);
            Random rng = new Random(42);
            for (int i = 0; i < 5_000; i++) {
                double x = (rng.nextDouble() - 0.5) * 100.0;
                double y = (rng.nextDouble() - 0.5) * 100.0;
                double z = (rng.nextDouble() - 0.5) * 100.0;
                double value = noise.sample(x, y, z);
                assertTrue(Double.isFinite(value), "DISTANCE 3D not finite: " + value);
                assertTrue(value >= -1.0 && value <= 3.0,
                        "DISTANCE 3D out of expected range: " + value);
            }
        }
    }

    @Nested
    @DisplayName("NoiseFactory.create() Tests")
    class FactoryCreateTests {

        @ParameterizedTest(name = "NoiseType.{0}")
        @EnumSource(value = NoiseType.class, names = {"CONSTANT"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("NoiseFactory.create() returns a functional noise for every non-CONSTANT type")
        void factoryCreateReturnsFunctionalNoise(NoiseType type) {
            NoiseFunction noise = NoiseFactory.create(KEY, type, SEED);
            assertNotNull(noise);
            double v1 = noise.sample(1.5, 2.5);
            double v2 = noise.sample(1.5, 2.5);
            assertEquals(v1, v2, 0.0, "Factory-created " + type + " is not deterministic");
        }

        @Test
        @DisplayName("NoiseFactory.create() with CONSTANT returns zero")
        void factoryCreateConstant() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.CONSTANT, SEED);
            assertEquals(0.0, noise.sample(5.0, 10.0), 0.0);
            assertEquals(0.0, noise.sample(5.0, 10.0, 15.0), 0.0);
        }
    }

    @Nested
    @DisplayName("Reconstructed Instance Determinism")
    class ReconstructedInstanceDeterminism {

        @Test
        @DisplayName("Two SimplexNoise instances with the same seed produce identical output")
        void simplexReconstructed() {
            SimplexNoise a = new SimplexNoise(KEY, SEED);
            SimplexNoise b = new SimplexNoise(KEY, SEED);
            for (int i = 0; i < 50; i++) {
                double x = i * 1.23;
                double z = i * 4.56;
                assertEquals(a.sample(x, z), b.sample(x, z), 0.0);
                assertEquals(a.sample(x, z, x + z), b.sample(x, z, x + z), 0.0);
            }
        }

        @Test
        @DisplayName("Two PerlinNoise instances with the same seed produce identical output")
        void perlinReconstructed() {
            PerlinNoise a = new PerlinNoise(KEY, SEED);
            PerlinNoise b = new PerlinNoise(KEY, SEED);
            for (int i = 0; i < 50; i++) {
                double x = i * 1.23;
                double z = i * 4.56;
                assertEquals(a.sample(x, z), b.sample(x, z), 0.0);
                assertEquals(a.sample(x, z, x + z), b.sample(x, z, x + z), 0.0);
            }
        }

        @Test
        @DisplayName("Two WhiteNoise instances with the same seed produce identical output")
        void whiteReconstructed() {
            WhiteNoise a = new WhiteNoise(KEY, SEED);
            WhiteNoise b = new WhiteNoise(KEY, SEED);
            for (int i = 0; i < 50; i++) {
                double x = i * 1.23;
                double z = i * 4.56;
                assertEquals(a.sample(x, z), b.sample(x, z), 0.0);
                assertEquals(a.sample(x, z, x + z), b.sample(x, z, x + z), 0.0);
            }
        }
    }
}
