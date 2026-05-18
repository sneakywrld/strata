package com.protectcord.strata.noise.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoiseMath Tests")
class NoiseMathTest {

    private static final double EPSILON = 1e-12;

    @Nested
    @DisplayName("lerp()")
    class LerpTests {

        @Test
        @DisplayName("lerp(0, 10, 0.5) = 5")
        void lerpMidpoint() {
            assertEquals(5.0, NoiseMath.lerp(0, 10, 0.5), EPSILON);
        }

        @Test
        @DisplayName("lerp(0, 10, 0) = 0")
        void lerpAtStart() {
            assertEquals(0.0, NoiseMath.lerp(0, 10, 0), EPSILON);
        }

        @Test
        @DisplayName("lerp(0, 10, 1) = 10")
        void lerpAtEnd() {
            assertEquals(10.0, NoiseMath.lerp(0, 10, 1), EPSILON);
        }

        @Test
        @DisplayName("lerp(3, 7, 0.25) = 4")
        void lerpQuarter() {
            assertEquals(4.0, NoiseMath.lerp(3, 7, 0.25), EPSILON);
        }

        @Test
        @DisplayName("lerp(-5, 5, 0.5) = 0")
        void lerpNegativeToPositive() {
            assertEquals(0.0, NoiseMath.lerp(-5, 5, 0.5), EPSILON);
        }

        @Test
        @DisplayName("lerp extrapolates beyond [0, 1] range")
        void lerpExtrapolates() {
            assertEquals(20.0, NoiseMath.lerp(0, 10, 2.0), EPSILON);
            assertEquals(-10.0, NoiseMath.lerp(0, 10, -1.0), EPSILON);
        }
    }

    @Nested
    @DisplayName("smoothstep()")
    class SmoothstepTests {

        @Test
        @DisplayName("smoothstep(0) = 0")
        void smoothstepAtZero() {
            assertEquals(0.0, NoiseMath.smoothstep(0), EPSILON);
        }

        @Test
        @DisplayName("smoothstep(1) = 1")
        void smoothstepAtOne() {
            assertEquals(1.0, NoiseMath.smoothstep(1), EPSILON);
        }

        @Test
        @DisplayName("smoothstep(0.5) = 0.5")
        void smoothstepAtHalf() {
            assertEquals(0.5, NoiseMath.smoothstep(0.5), EPSILON);
        }

        @Test
        @DisplayName("smoothstep is monotonically increasing on [0, 1]")
        void smoothstepMonotonic() {
            double prev = NoiseMath.smoothstep(0.0);
            for (int i = 1; i <= 100; i++) {
                double t = i / 100.0;
                double current = NoiseMath.smoothstep(t);
                assertTrue(current >= prev,
                        "smoothstep not monotonic at t=" + t + ": " + prev + " -> " + current);
                prev = current;
            }
        }

        @Test
        @DisplayName("smoothstep output is in [0, 1] for input in [0, 1]")
        void smoothstepOutputRange() {
            for (int i = 0; i <= 100; i++) {
                double t = i / 100.0;
                double v = NoiseMath.smoothstep(t);
                assertTrue(v >= 0.0 && v <= 1.0,
                        "smoothstep(" + t + ") = " + v + " out of [0, 1]");
            }
        }
    }

    @Nested
    @DisplayName("smootherstep()")
    class SmootherstepTests {

        @Test
        @DisplayName("smootherstep(0) = 0")
        void smootherstepAtZero() {
            assertEquals(0.0, NoiseMath.smootherstep(0), EPSILON);
        }

        @Test
        @DisplayName("smootherstep(1) = 1")
        void smootherstepAtOne() {
            assertEquals(1.0, NoiseMath.smootherstep(1), EPSILON);
        }

        @Test
        @DisplayName("smootherstep(0.5) = 0.5")
        void smootherstepAtHalf() {
            assertEquals(0.5, NoiseMath.smootherstep(0.5), EPSILON);
        }

        @Test
        @DisplayName("smootherstep is monotonically increasing on [0, 1]")
        void smootherstepMonotonic() {
            double prev = NoiseMath.smootherstep(0.0);
            for (int i = 1; i <= 100; i++) {
                double t = i / 100.0;
                double current = NoiseMath.smootherstep(t);
                assertTrue(current >= prev,
                        "smootherstep not monotonic at t=" + t);
                prev = current;
            }
        }

        @Test
        @DisplayName("smootherstep output is in [0, 1] for input in [0, 1]")
        void smootherstepOutputRange() {
            for (int i = 0; i <= 100; i++) {
                double t = i / 100.0;
                double v = NoiseMath.smootherstep(t);
                assertTrue(v >= 0.0 && v <= 1.0,
                        "smootherstep(" + t + ") = " + v + " out of [0, 1]");
            }
        }
    }

    @Nested
    @DisplayName("clamp()")
    class ClampTests {

        @Test
        @DisplayName("clamp(5, 0, 10) = 5 (within range)")
        void clampWithinRange() {
            assertEquals(5.0, NoiseMath.clamp(5, 0, 10), EPSILON);
        }

        @Test
        @DisplayName("clamp(-1, 0, 10) = 0 (below min)")
        void clampBelowMin() {
            assertEquals(0.0, NoiseMath.clamp(-1, 0, 10), EPSILON);
        }

        @Test
        @DisplayName("clamp(11, 0, 10) = 10 (above max)")
        void clampAboveMax() {
            assertEquals(10.0, NoiseMath.clamp(11, 0, 10), EPSILON);
        }

        @Test
        @DisplayName("clamp at exact min boundary")
        void clampAtMin() {
            assertEquals(0.0, NoiseMath.clamp(0, 0, 10), EPSILON);
        }

        @Test
        @DisplayName("clamp at exact max boundary")
        void clampAtMax() {
            assertEquals(10.0, NoiseMath.clamp(10, 0, 10), EPSILON);
        }

        @Test
        @DisplayName("clamp with negative range")
        void clampNegativeRange() {
            assertEquals(-5.0, NoiseMath.clamp(-5, -10, 0), EPSILON);
            assertEquals(-10.0, NoiseMath.clamp(-15, -10, 0), EPSILON);
            assertEquals(0.0, NoiseMath.clamp(5, -10, 0), EPSILON);
        }
    }

    @Nested
    @DisplayName("fastFloor()")
    class FastFloorTests {

        @Test
        @DisplayName("fastFloor(1.7) = 1")
        void fastFloorPositiveFraction() {
            assertEquals(1, NoiseMath.fastFloor(1.7));
        }

        @Test
        @DisplayName("fastFloor(-0.3) = -1")
        void fastFloorNegativeFraction() {
            assertEquals(-1, NoiseMath.fastFloor(-0.3));
        }

        @Test
        @DisplayName("fastFloor(0) = 0")
        void fastFloorZero() {
            assertEquals(0, NoiseMath.fastFloor(0.0));
        }

        @Test
        @DisplayName("fastFloor(5.0) = 5")
        void fastFloorExactPositive() {
            assertEquals(5, NoiseMath.fastFloor(5.0));
        }

        @Test
        @DisplayName("fastFloor(-3.0) = -3")
        void fastFloorExactNegative() {
            assertEquals(-3, NoiseMath.fastFloor(-3.0));
        }

        @Test
        @DisplayName("fastFloor(0.999) = 0")
        void fastFloorJustBelowOne() {
            assertEquals(0, NoiseMath.fastFloor(0.999));
        }

        @Test
        @DisplayName("fastFloor(-1.001) = -2")
        void fastFloorJustBelowNegOne() {
            assertEquals(-2, NoiseMath.fastFloor(-1.001));
        }

        @ParameterizedTest(name = "fastFloor({0}) = {1}")
        @CsvSource({
                "2.5, 2",
                "-2.5, -3",
                "100.1, 100",
                "-100.1, -101",
                "0.0001, 0",
                "-0.0001, -1"
        })
        @DisplayName("fastFloor parametric cases")
        void fastFloorParametric(double input, int expected) {
            assertEquals(expected, NoiseMath.fastFloor(input));
        }
    }

    @Nested
    @DisplayName("hash()")
    class HashTests {

        @Test
        @DisplayName("hash is deterministic for same inputs")
        void hashIsDeterministic() {
            long h1 = NoiseMath.hash(42L, 10, 20);
            long h2 = NoiseMath.hash(42L, 10, 20);
            assertEquals(h1, h2);
        }

        @Test
        @DisplayName("hash with different seed produces different output")
        void hashDifferentSeed() {
            long h1 = NoiseMath.hash(42L, 10, 20);
            long h2 = NoiseMath.hash(99L, 10, 20);
            assertNotEquals(h1, h2);
        }

        @Test
        @DisplayName("hash with different x produces different output")
        void hashDifferentX() {
            long h1 = NoiseMath.hash(42L, 10, 20);
            long h2 = NoiseMath.hash(42L, 11, 20);
            assertNotEquals(h1, h2);
        }

        @Test
        @DisplayName("hash with different y produces different output")
        void hashDifferentY() {
            long h1 = NoiseMath.hash(42L, 10, 20);
            long h2 = NoiseMath.hash(42L, 10, 21);
            assertNotEquals(h1, h2);
        }

        @Test
        @DisplayName("3D hash is deterministic")
        void hash3dIsDeterministic() {
            long h1 = NoiseMath.hash(42L, 10, 20, 30);
            long h2 = NoiseMath.hash(42L, 10, 20, 30);
            assertEquals(h1, h2);
        }

        @Test
        @DisplayName("3D hash with different z produces different output")
        void hash3dDifferentZ() {
            long h1 = NoiseMath.hash(42L, 10, 20, 30);
            long h2 = NoiseMath.hash(42L, 10, 20, 31);
            assertNotEquals(h1, h2);
        }

        @Test
        @DisplayName("hash produces varied distribution")
        void hashDistribution() {
            int positiveCount = 0;
            for (int i = 0; i < 1000; i++) {
                long h = NoiseMath.hash(12345L, i, i * 7);
                if (h > 0) positiveCount++;
            }
            assertTrue(positiveCount > 300 && positiveCount < 700,
                    "Hash distribution is heavily skewed: " + positiveCount + "/1000 positive");
        }
    }

    @Nested
    @DisplayName("hashToDouble()")
    class HashToDoubleTests {

        @Test
        @DisplayName("hashToDouble result is always in [-1, 1]")
        void hashToDoubleRange() {
            for (int i = 0; i < 10_000; i++) {
                long hash = NoiseMath.hash(42L, i, i * 3);
                double value = NoiseMath.hashToDouble(hash);
                assertTrue(value >= -1.0 && value <= 1.0,
                        "hashToDouble out of range: " + value + " for hash " + hash);
            }
        }

        @Test
        @DisplayName("hashToDouble produces varied output")
        void hashToDoubleVariation() {
            double first = NoiseMath.hashToDouble(NoiseMath.hash(42L, 0, 0));
            int variations = 0;
            for (int i = 1; i < 100; i++) {
                double v = NoiseMath.hashToDouble(NoiseMath.hash(42L, i, i));
                if (v != first) variations++;
            }
            assertTrue(variations > 80, "hashToDouble shows too little variation: " + variations + "/99");
        }

        @Test
        @DisplayName("hashToDouble of zero hash is deterministic")
        void hashToDoubleOfZero() {
            double v1 = NoiseMath.hashToDouble(0L);
            double v2 = NoiseMath.hashToDouble(0L);
            assertEquals(v1, v2, 0.0);
            assertTrue(v1 >= -1.0 && v1 <= 1.0);
        }

        @Test
        @DisplayName("hashToDouble covers both negative and positive range")
        void hashToDoubleCoversBothSigns() {
            int negativeCount = 0;
            int positiveCount = 0;
            for (int i = 0; i < 1000; i++) {
                long hash = NoiseMath.hash(7777L, i, i * 13);
                double v = NoiseMath.hashToDouble(hash);
                if (v < 0) negativeCount++;
                if (v > 0) positiveCount++;
            }
            assertTrue(negativeCount > 200, "Too few negative values: " + negativeCount);
            assertTrue(positiveCount > 200, "Too few positive values: " + positiveCount);
        }

        @Test
        @DisplayName("hashToDouble of extreme long values stays in range")
        void hashToDoubleExtremeValues() {
            assertTrue(NoiseMath.hashToDouble(Long.MAX_VALUE) >= -1.0);
            assertTrue(NoiseMath.hashToDouble(Long.MAX_VALUE) <= 1.0);
            assertTrue(NoiseMath.hashToDouble(Long.MIN_VALUE) >= -1.0);
            assertTrue(NoiseMath.hashToDouble(Long.MIN_VALUE) <= 1.0);
        }
    }
}
