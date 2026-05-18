package com.protectcord.strata.noise.cache;

import com.protectcord.strata.api.core.NamespacedKey;
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.noise.NoiseFactory;
import com.protectcord.strata.noise.algorithm.SimplexNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoiseCache Tests")
class NoiseCacheTest {

    private static final long SEED = 12345L;
    private static final NamespacedKey KEY = NamespacedKey.strata("test_noise");

    @Test
    @DisplayName("2D cache hit returns same value as first call")
    void cacheHitReturnsSameValue2D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        double first = cached.sample(10.5, 20.5);
        double second = cached.sample(10.5, 20.5);
        assertEquals(first, second, 0.0);
    }

    @Test
    @DisplayName("3D cache hit returns same value as first call")
    void cacheHitReturnsSameValue3D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        double first = cached.sample(10.5, 15.0, 20.5);
        double second = cached.sample(10.5, 15.0, 20.5);
        assertEquals(first, second, 0.0);
    }

    @Test
    @DisplayName("Cached result matches delegate for 2D sampling")
    void cachedResultMatchesDelegate2D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        for (int i = 0; i < 50; i++) {
            double x = i * 1.5;
            double z = i * 2.5;
            assertEquals(base.sample(x, z), cached.sample(x, z), 0.0);
        }
    }

    @Test
    @DisplayName("Cached result matches delegate for 3D sampling")
    void cachedResultMatchesDelegate3D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        for (int i = 0; i < 50; i++) {
            double x = i * 1.5;
            double y = i * 0.7;
            double z = i * 2.5;
            assertEquals(base.sample(x, y, z), cached.sample(x, y, z), 0.0);
        }
    }

    @Test
    @DisplayName("Different 2D coordinates return different delegate values")
    void differentCoordinatesReturnDelegateValues2D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        double v1 = cached.sample(1.0, 2.0);
        double v2 = cached.sample(50.0, 100.0);
        assertEquals(base.sample(1.0, 2.0), v1, 0.0);
        assertEquals(base.sample(50.0, 100.0), v2, 0.0);
    }

    @Test
    @DisplayName("Different 3D coordinates return different delegate values")
    void differentCoordinatesReturnDelegateValues3D() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        double v1 = cached.sample(1.0, 2.0, 3.0);
        double v2 = cached.sample(50.0, 60.0, 70.0);
        assertEquals(base.sample(1.0, 2.0, 3.0), v1, 0.0);
        assertEquals(base.sample(50.0, 60.0, 70.0), v2, 0.0);
    }

    @Test
    @DisplayName("invalidate() clears the cache")
    void invalidateClearsCache() {
        CallCountingNoise counter = new CallCountingNoise(KEY);
        NoiseCache cached = new NoiseCache(counter, 1024);

        cached.sample(1.0, 2.0);
        int callsAfterFirst = counter.callCount2D;

        cached.sample(1.0, 2.0);
        assertEquals(callsAfterFirst, counter.callCount2D, "Second call should hit cache, not delegate");

        cached.invalidate();

        cached.sample(1.0, 2.0);
        assertEquals(callsAfterFirst + 1, counter.callCount2D,
                "After invalidate, delegate should be called again");
    }

    @Test
    @DisplayName("invalidate() clears 3D cache")
    void invalidateClears3DCache() {
        CallCountingNoise counter = new CallCountingNoise(KEY);
        NoiseCache cached = new NoiseCache(counter, 1024);

        cached.sample(1.0, 2.0, 3.0);
        int callsAfterFirst = counter.callCount3D;

        cached.sample(1.0, 2.0, 3.0);
        assertEquals(callsAfterFirst, counter.callCount3D, "Second 3D call should hit cache");

        cached.invalidate();

        cached.sample(1.0, 2.0, 3.0);
        assertEquals(callsAfterFirst + 1, counter.callCount3D,
                "After invalidate, 3D delegate should be called again");
    }

    @Test
    @DisplayName("key() delegates to the wrapped function")
    void keyDelegatesToWrapped() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        assertEquals(KEY, cached.key());
    }

    @Test
    @DisplayName("minValue() delegates to the wrapped function")
    void minValueDelegatesToWrapped() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        assertEquals(base.minValue(), cached.minValue(), 0.0);
    }

    @Test
    @DisplayName("maxValue() delegates to the wrapped function")
    void maxValueDelegatesToWrapped() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base, 1024);
        assertEquals(base.maxValue(), cached.maxValue(), 0.0);
    }

    @Test
    @DisplayName("NoiseFactory.cached() returns a NoiseCache instance")
    void factoryCachedReturnsNoiseCache() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = NoiseFactory.cached(base, 512);
        assertNotNull(cached);
        assertInstanceOf(NoiseCache.class, cached);
    }

    @Test
    @DisplayName("NoiseFactory.cached() result returns correct values")
    void factoryCachedReturnsCorrectValues() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = NoiseFactory.cached(base, 512);
        for (int i = 0; i < 50; i++) {
            double x = i * 1.3;
            double z = i * 2.7;
            assertEquals(base.sample(x, z), cached.sample(x, z), 0.0);
        }
    }

    @Test
    @DisplayName("Default constructor uses default maxSize")
    void defaultConstructor() {
        NoiseFunction base = new SimplexNoise(KEY, SEED);
        NoiseCache cached = new NoiseCache(base);
        double v1 = cached.sample(1.0, 2.0);
        double v2 = cached.sample(1.0, 2.0);
        assertEquals(v1, v2, 0.0);
    }

    @Test
    @DisplayName("Cache with constant noise delegates key/min/max")
    void cacheWithConstantNoise() {
        NoiseFunction constant = NoiseFactory.constant(KEY, 0.42);
        NoiseCache cached = new NoiseCache(constant, 1024);
        assertEquals(KEY, cached.key());
        assertEquals(0.42, cached.minValue(), 0.0);
        assertEquals(0.42, cached.maxValue(), 0.0);
        assertEquals(0.42, cached.sample(99.0, 99.0), 0.0);
    }

    private static class CallCountingNoise implements NoiseFunction {
        private final NamespacedKey key;
        int callCount2D = 0;
        int callCount3D = 0;

        CallCountingNoise(NamespacedKey key) {
            this.key = key;
        }

        @Override
        public NamespacedKey key() {
            return key;
        }

        @Override
        public double sample(double x, double z) {
            callCount2D++;
            return x + z;
        }

        @Override
        public double sample(double x, double y, double z) {
            callCount3D++;
            return x + y + z;
        }

        @Override
        public double minValue() {
            return -1.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }
    }
}
