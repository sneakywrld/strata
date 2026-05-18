package com.protectcord.strata.noise;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.FractalSettings;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.noise.NoiseType;
import com.protectcord.strata.noise.algorithm.*;
import com.protectcord.strata.noise.cache.NoiseCache;
import com.protectcord.strata.noise.fractal.FractalNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoiseFactory Tests")
class NoiseFactoryTest {

    private static final long SEED = 12345L;
    private static final NamespacedKey KEY = NamespacedKey.strata("test_noise");

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("PERLIN creates a PerlinNoise instance")
        void createPerlin() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.PERLIN, SEED);
            assertInstanceOf(PerlinNoise.class, noise);
        }

        @Test
        @DisplayName("SIMPLEX creates a SimplexNoise instance")
        void createSimplex() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.SIMPLEX, SEED);
            assertInstanceOf(SimplexNoise.class, noise);
        }

        @Test
        @DisplayName("OPEN_SIMPLEX_2 creates an OpenSimplex2Noise instance")
        void createOpenSimplex2() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.OPEN_SIMPLEX_2, SEED);
            assertInstanceOf(OpenSimplex2Noise.class, noise);
        }

        @Test
        @DisplayName("OPEN_SIMPLEX_2S creates an OpenSimplex2Noise instance")
        void createOpenSimplex2S() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.OPEN_SIMPLEX_2S, SEED);
            assertInstanceOf(OpenSimplex2Noise.class, noise);
        }

        @Test
        @DisplayName("CELLULAR creates a CellularNoise instance")
        void createCellular() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.CELLULAR, SEED);
            assertInstanceOf(CellularNoise.class, noise);
        }

        @Test
        @DisplayName("VALUE creates a ValueNoise instance")
        void createValue() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.VALUE, SEED);
            assertInstanceOf(ValueNoise.class, noise);
        }

        @Test
        @DisplayName("RIDGED_MULTI creates a RidgedMultifractalNoise instance")
        void createRidgedMulti() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.RIDGED_MULTI, SEED);
            assertInstanceOf(RidgedMultifractalNoise.class, noise);
        }

        @Test
        @DisplayName("WHITE creates a WhiteNoise instance")
        void createWhite() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.WHITE, SEED);
            assertInstanceOf(WhiteNoise.class, noise);
        }

        @Test
        @DisplayName("CONSTANT creates a noise function returning zero")
        void createConstant() {
            NoiseFunction noise = NoiseFactory.create(KEY, NoiseType.CONSTANT, SEED);
            assertNotNull(noise);
            assertEquals(0.0, noise.sample(1.0, 2.0), 0.0);
            assertEquals(0.0, noise.sample(1.0, 2.0, 3.0), 0.0);
        }

        @ParameterizedTest(name = "NoiseType.{0} creates a non-null function")
        @EnumSource(NoiseType.class)
        @DisplayName("Every NoiseType produces a non-null NoiseFunction")
        void everyTypeProducesNonNull(NoiseType type) {
            NoiseFunction noise = NoiseFactory.create(KEY, type, SEED);
            assertNotNull(noise);
        }

        @ParameterizedTest(name = "NoiseType.{0} returns a function with a valid key")
        @EnumSource(NoiseType.class)
        @DisplayName("Every created function has the supplied key")
        void everyTypeHasCorrectKey(NoiseType type) {
            NoiseFunction noise = NoiseFactory.create(KEY, type, SEED);
            assertEquals(KEY, noise.key());
        }
    }

    @Nested
    @DisplayName("fractal()")
    class FractalTests {

        @Test
        @DisplayName("fractal() returns a FractalNoise instance")
        void fractalReturnsFractalNoise() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseFunction fractal = NoiseFactory.fractal(KEY, base, FractalSettings.defaultSettings());
            assertInstanceOf(FractalNoise.class, fractal);
        }

        @Test
        @DisplayName("fractal() result is functional and deterministic")
        void fractalIsFunctional() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseFunction fractal = NoiseFactory.fractal(KEY, base, FractalSettings.defaultSettings());
            double v1 = fractal.sample(10.0, 20.0);
            double v2 = fractal.sample(10.0, 20.0);
            assertEquals(v1, v2, 0.0);
        }

        @Test
        @DisplayName("fractal() result has the supplied key")
        void fractalHasCorrectKey() {
            NamespacedKey fractalKey = NamespacedKey.strata("fractal_key");
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseFunction fractal = NoiseFactory.fractal(fractalKey, base, FractalSettings.defaultSettings());
            assertEquals(fractalKey, fractal.key());
        }

        @Test
        @DisplayName("fractal() with custom settings works")
        void fractalWithCustomSettings() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            FractalSettings custom = new FractalSettings(6, 0.5, 1.5, 3.0, 0.4);
            NoiseFunction fractal = NoiseFactory.fractal(KEY, base, custom);
            assertNotNull(fractal);
            double v = fractal.sample(5.0, 10.0);
            assertTrue(Double.isFinite(v));
        }
    }

    @Nested
    @DisplayName("cached()")
    class CachedTests {

        @Test
        @DisplayName("cached() returns a NoiseCache instance")
        void cachedReturnsNoiseCache() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseCache cached = NoiseFactory.cached(base, 1024);
            assertInstanceOf(NoiseCache.class, cached);
        }

        @Test
        @DisplayName("cached() result matches delegate output")
        void cachedMatchesDelegate() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseCache cached = NoiseFactory.cached(base, 1024);
            assertEquals(base.sample(5.0, 10.0), cached.sample(5.0, 10.0), 0.0);
            assertEquals(base.sample(5.0, 10.0, 15.0), cached.sample(5.0, 10.0, 15.0), 0.0);
        }

        @Test
        @DisplayName("cached() preserves the delegate key")
        void cachedPreservesKey() {
            NoiseFunction base = new SimplexNoise(KEY, SEED);
            NoiseCache cached = NoiseFactory.cached(base, 1024);
            assertEquals(KEY, cached.key());
        }
    }

    @Nested
    @DisplayName("constant()")
    class ConstantTests {

        @Test
        @DisplayName("constant() returns the specified value for 2D sampling")
        void constantReturnsValue2D() {
            NoiseFunction constant = NoiseFactory.constant(KEY, 0.42);
            assertEquals(0.42, constant.sample(0, 0), 0.0);
            assertEquals(0.42, constant.sample(100, 200), 0.0);
            assertEquals(0.42, constant.sample(-50, -50), 0.0);
        }

        @Test
        @DisplayName("constant() returns the specified value for 3D sampling")
        void constantReturnsValue3D() {
            NoiseFunction constant = NoiseFactory.constant(KEY, -0.7);
            assertEquals(-0.7, constant.sample(0, 0, 0), 0.0);
            assertEquals(-0.7, constant.sample(100, 200, 300), 0.0);
        }

        @Test
        @DisplayName("constant() returns the value everywhere regardless of coordinates")
        void constantReturnsSameValueEverywhere() {
            NoiseFunction constant = NoiseFactory.constant(KEY, 0.5);
            for (int i = -50; i <= 50; i++) {
                assertEquals(0.5, constant.sample(i, i * 2), 0.0);
                assertEquals(0.5, constant.sample(i, i * 2, i * 3), 0.0);
            }
        }

        @Test
        @DisplayName("constant() has the supplied key")
        void constantHasKey() {
            NoiseFunction constant = NoiseFactory.constant(KEY, 0.5);
            assertEquals(KEY, constant.key());
        }

        @Test
        @DisplayName("constant() minValue and maxValue equal the constant")
        void constantMinMaxEqualValue() {
            NoiseFunction constant = NoiseFactory.constant(KEY, 0.42);
            assertEquals(0.42, constant.minValue(), 0.0);
            assertEquals(0.42, constant.maxValue(), 0.0);
        }

        @Test
        @DisplayName("constant(0) returns zero everywhere")
        void constantZero() {
            NoiseFunction constant = NoiseFactory.constant(KEY, 0.0);
            assertEquals(0.0, constant.sample(999, 999), 0.0);
            assertEquals(0.0, constant.sample(999, 999, 999), 0.0);
        }

        @Test
        @DisplayName("constant(-1) returns -1 everywhere")
        void constantNegativeOne() {
            NoiseFunction constant = NoiseFactory.constant(KEY, -1.0);
            assertEquals(-1.0, constant.sample(0, 0), 0.0);
            assertEquals(-1.0, constant.minValue(), 0.0);
            assertEquals(-1.0, constant.maxValue(), 0.0);
        }
    }
}
