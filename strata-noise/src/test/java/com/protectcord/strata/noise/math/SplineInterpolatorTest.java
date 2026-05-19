package com.protectcord.strata.noise.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SplineInterpolator} natural cubic spline interpolation.
 */
@DisplayName("SplineInterpolator Tests")
class SplineInterpolatorTest {

    private static final double EPSILON = 1e-9;

    // ================================================================ construction

    @Nested
    @DisplayName("Construction validation")
    class ConstructionTests {

        @Test
        @DisplayName("Two control points creates valid spline")
        void twoControlPoints_succeeds() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{0.0, 1.0},
                    new double[]{0.0, 10.0}
            );
            assertEquals(2, spline.controlPointCount());
            assertEquals(0.0, spline.minX(), EPSILON);
            assertEquals(1.0, spline.maxX(), EPSILON);
        }

        @Test
        @DisplayName("Single control point throws IllegalArgumentException")
        void singlePoint_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SplineInterpolator(new double[]{1.0}, new double[]{5.0}));
        }

        @Test
        @DisplayName("Empty arrays throw IllegalArgumentException")
        void emptyArrays_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SplineInterpolator(new double[]{}, new double[]{}));
        }

        @Test
        @DisplayName("Mismatched array lengths throw IllegalArgumentException")
        void mismatchedLengths_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SplineInterpolator(
                            new double[]{0.0, 1.0, 2.0},
                            new double[]{0.0, 1.0}
                    ));
        }

        @Test
        @DisplayName("Duplicate x values throw IllegalArgumentException")
        void duplicateXValues_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SplineInterpolator(
                            new double[]{0.0, 1.0, 1.0, 2.0},
                            new double[]{0.0, 5.0, 5.0, 10.0}
                    ));
        }

        @Test
        @DisplayName("Non-increasing x values throw IllegalArgumentException")
        void nonIncreasingX_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SplineInterpolator(
                            new double[]{0.0, 3.0, 2.0, 4.0},
                            new double[]{0.0, 5.0, 3.0, 10.0}
                    ));
        }

        @Test
        @DisplayName("Many control points creates valid spline")
        void manyControlPoints_succeeds() {
            double[] xs = new double[100];
            double[] ys = new double[100];
            for (int i = 0; i < 100; i++) {
                xs[i] = i * 0.1;
                ys[i] = Math.sin(xs[i]);
            }
            SplineInterpolator spline = new SplineInterpolator(xs, ys);
            assertEquals(100, spline.controlPointCount());
            assertEquals(0.0, spline.minX(), EPSILON);
            assertEquals(9.9, spline.maxX(), EPSILON);
        }
    }

    // ================================================================ linear interpolation (2 points)

    @Nested
    @DisplayName("Linear interpolation (2 control points)")
    class LinearInterpolationTests {

        private final SplineInterpolator linear = new SplineInterpolator(
                new double[]{0.0, 10.0},
                new double[]{0.0, 100.0}
        );

        @Test
        @DisplayName("Evaluates exactly at start point")
        void evaluateAtStart() {
            assertEquals(0.0, linear.evaluate(0.0), EPSILON);
        }

        @Test
        @DisplayName("Evaluates exactly at end point")
        void evaluateAtEnd() {
            assertEquals(100.0, linear.evaluate(10.0), EPSILON);
        }

        @Test
        @DisplayName("Evaluates at midpoint")
        void evaluateAtMidpoint() {
            assertEquals(50.0, linear.evaluate(5.0), EPSILON);
        }

        @Test
        @DisplayName("Evaluates at quarter point")
        void evaluateAtQuarter() {
            assertEquals(25.0, linear.evaluate(2.5), EPSILON);
        }

        @Test
        @DisplayName("Evaluates at three-quarter point")
        void evaluateAtThreeQuarter() {
            assertEquals(75.0, linear.evaluate(7.5), EPSILON);
        }

        @Test
        @DisplayName("Two-point spline produces exactly linear results")
        void twoPointsAreExactlyLinear() {
            // With 2 control points the spline degenerates to a straight line
            for (int i = 0; i <= 100; i++) {
                double x = i / 10.0;
                double expected = x * 10.0;
                assertEquals(expected, linear.evaluate(x), EPSILON,
                        "Should be linear at x=" + x);
            }
        }

        @Test
        @DisplayName("Two-point spline with negative slope")
        void negativeSlope() {
            SplineInterpolator descending = new SplineInterpolator(
                    new double[]{0.0, 5.0},
                    new double[]{100.0, 0.0}
            );
            assertEquals(100.0, descending.evaluate(0.0), EPSILON);
            assertEquals(50.0, descending.evaluate(2.5), EPSILON);
            assertEquals(0.0, descending.evaluate(5.0), EPSILON);
        }

        @Test
        @DisplayName("Two-point spline with constant y values")
        void constantValues() {
            SplineInterpolator flat = new SplineInterpolator(
                    new double[]{-1.0, 1.0},
                    new double[]{42.0, 42.0}
            );
            assertEquals(42.0, flat.evaluate(-1.0), EPSILON);
            assertEquals(42.0, flat.evaluate(0.0), EPSILON);
            assertEquals(42.0, flat.evaluate(1.0), EPSILON);
        }
    }

    // ================================================================ cubic spline (4+ control points)

    @Nested
    @DisplayName("Cubic spline interpolation (4+ control points)")
    class CubicSplineTests {

        private final SplineInterpolator cubic = new SplineInterpolator(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{0.0, 1.0, 0.0, 1.0}
        );

        @Test
        @DisplayName("Evaluates exactly at each control point")
        void evaluatesAtControlPoints() {
            assertEquals(0.0, cubic.evaluate(0.0), EPSILON);
            assertEquals(1.0, cubic.evaluate(1.0), EPSILON);
            assertEquals(0.0, cubic.evaluate(2.0), EPSILON);
            assertEquals(1.0, cubic.evaluate(3.0), EPSILON);
        }

        @Test
        @DisplayName("Intermediate values are smooth (not linear)")
        void intermediateValuesAreSmooth() {
            // Cubic spline at x=0.5 should NOT be exactly 0.5 (that would be linear)
            double val = cubic.evaluate(0.5);
            assertNotEquals(0.5, val, 0.01,
                    "Cubic spline should not produce exactly linear interpolation");
        }

        @Test
        @DisplayName("Spline is continuous across segments")
        void continuityAcrossSegments() {
            // Check values just before and after knot points are close
            double justBefore1 = cubic.evaluate(0.999);
            double justAfter1 = cubic.evaluate(1.001);
            assertEquals(justBefore1, justAfter1, 0.01,
                    "Spline should be continuous at knot x=1.0");

            double justBefore2 = cubic.evaluate(1.999);
            double justAfter2 = cubic.evaluate(2.001);
            assertEquals(justBefore2, justAfter2, 0.01,
                    "Spline should be continuous at knot x=2.0");
        }

        @Test
        @DisplayName("Five-point spline interpolates accurately")
        void fivePointSpline() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{-2.0, -1.0, 0.0, 1.0, 2.0},
                    new double[]{4.0, 1.0, 0.0, 1.0, 4.0}
            );
            // Control points are on a parabola y = x^2
            assertEquals(4.0, spline.evaluate(-2.0), EPSILON);
            assertEquals(0.0, spline.evaluate(0.0), EPSILON);
            assertEquals(4.0, spline.evaluate(2.0), EPSILON);

            // Intermediate points should be close to parabola
            double atHalf = spline.evaluate(0.5);
            assertTrue(atHalf > 0.0 && atHalf < 1.0,
                    "Value at x=0.5 should be between 0 and 1, was: " + atHalf);
        }

        @Test
        @DisplayName("Many-point spline approximates sine curve")
        void sineApproximation() {
            int n = 20;
            double[] xs = new double[n];
            double[] ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = i * Math.PI / (n - 1);
                ys[i] = Math.sin(xs[i]);
            }
            SplineInterpolator sinSpline = new SplineInterpolator(xs, ys);

            // Sample between control points and check approximation quality
            for (int i = 0; i < 50; i++) {
                double x = i * Math.PI / 50.0;
                double splineVal = sinSpline.evaluate(x);
                double expected = Math.sin(x);
                assertEquals(expected, splineVal, 0.01,
                        "Spline approximation of sin should be within 0.01 at x=" + x);
            }
        }

        @Test
        @DisplayName("Six-point spline with varied data")
        void sixPointVariedData() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{0.0, 2.0, 4.0, 6.0, 8.0, 10.0},
                    new double[]{0.0, 5.0, 3.0, 8.0, 2.0, 7.0}
            );
            // Verify all control points
            assertEquals(0.0, spline.evaluate(0.0), EPSILON);
            assertEquals(5.0, spline.evaluate(2.0), EPSILON);
            assertEquals(3.0, spline.evaluate(4.0), EPSILON);
            assertEquals(8.0, spline.evaluate(6.0), EPSILON);
            assertEquals(2.0, spline.evaluate(8.0), EPSILON);
            assertEquals(7.0, spline.evaluate(10.0), EPSILON);
            assertEquals(6, spline.controlPointCount());
        }
    }

    // ================================================================ out-of-range queries

    @Nested
    @DisplayName("Out-of-range queries (extrapolation)")
    class ExtrapolationTests {

        private final SplineInterpolator spline = new SplineInterpolator(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 15.0, 25.0}
        );

        @Test
        @DisplayName("Query below minimum x uses first segment")
        void queryBelowMinX() {
            // Extrapolation should not crash; uses the first polynomial segment
            double result = spline.evaluate(-1.0);
            assertFalse(Double.isNaN(result), "Result should not be NaN for x below range");
            assertFalse(Double.isInfinite(result), "Result should not be infinite for x below range");
        }

        @Test
        @DisplayName("Query above maximum x uses last segment")
        void queryAboveMaxX() {
            double result = spline.evaluate(5.0);
            assertFalse(Double.isNaN(result), "Result should not be NaN for x above range");
            assertFalse(Double.isInfinite(result), "Result should not be infinite for x above range");
        }

        @Test
        @DisplayName("Query at exact minimum x returns first y")
        void queryAtMinX() {
            assertEquals(10.0, spline.evaluate(0.0), EPSILON);
        }

        @Test
        @DisplayName("Query at exact maximum x returns last y")
        void queryAtMaxX() {
            assertEquals(25.0, spline.evaluate(3.0), EPSILON);
        }

        @Test
        @DisplayName("Far below range still evaluates without error")
        void farBelowRange() {
            double result = spline.evaluate(-1000.0);
            assertFalse(Double.isNaN(result));
        }

        @Test
        @DisplayName("Far above range still evaluates without error")
        void farAboveRange() {
            double result = spline.evaluate(1000.0);
            assertFalse(Double.isNaN(result));
        }
    }

    // ================================================================ control point metadata

    @Nested
    @DisplayName("Control point metadata")
    class MetadataTests {

        @Test
        @DisplayName("controlPointCount returns correct count")
        void controlPointCount() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{1.0, 2.0, 3.0, 4.0, 5.0},
                    new double[]{10.0, 20.0, 30.0, 40.0, 50.0}
            );
            assertEquals(5, spline.controlPointCount());
        }

        @Test
        @DisplayName("minX returns first x value")
        void minXReturnsFirst() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{-5.0, 0.0, 5.0},
                    new double[]{1.0, 2.0, 3.0}
            );
            assertEquals(-5.0, spline.minX(), EPSILON);
        }

        @Test
        @DisplayName("maxX returns last x value")
        void maxXReturnsLast() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{-5.0, 0.0, 5.0},
                    new double[]{1.0, 2.0, 3.0}
            );
            assertEquals(5.0, spline.maxX(), EPSILON);
        }
    }

    // ================================================================ immutability

    @Nested
    @DisplayName("Immutability guarantees")
    class ImmutabilityTests {

        @Test
        @DisplayName("Mutating input arrays after construction has no effect")
        void inputArrayMutationSafe() {
            double[] xs = {0.0, 1.0, 2.0};
            double[] ys = {0.0, 5.0, 10.0};
            SplineInterpolator spline = new SplineInterpolator(xs, ys);

            // Mutate the original arrays
            xs[1] = 999.0;
            ys[1] = 999.0;

            // Spline should still evaluate correctly with original data
            assertEquals(5.0, spline.evaluate(1.0), EPSILON,
                    "Spline should use defensive copies of input arrays");
        }
    }

    // ================================================================ edge cases

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Closely spaced control points")
        void closelySpacedPoints() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{0.0, 0.001, 0.002, 0.003},
                    new double[]{0.0, 1.0, 0.0, 1.0}
            );
            // Should not produce NaN or Inf despite tiny intervals
            double result = spline.evaluate(0.0015);
            assertFalse(Double.isNaN(result), "Should handle closely spaced points");
            assertFalse(Double.isInfinite(result), "Should not overflow with tiny intervals");
        }

        @Test
        @DisplayName("Negative x range works correctly")
        void negativeXRange() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{-10.0, -5.0, 0.0},
                    new double[]{100.0, 50.0, 0.0}
            );
            assertEquals(100.0, spline.evaluate(-10.0), EPSILON);
            assertEquals(0.0, spline.evaluate(0.0), EPSILON);
            // Value at midpoint should be between endpoints for this monotonic data
            double midVal = spline.evaluate(-5.0);
            assertEquals(50.0, midVal, EPSILON);
        }

        @Test
        @DisplayName("Large y values are handled correctly")
        void largeYValues() {
            SplineInterpolator spline = new SplineInterpolator(
                    new double[]{0.0, 1.0, 2.0},
                    new double[]{1e12, -1e12, 1e12}
            );
            assertEquals(1e12, spline.evaluate(0.0), 1.0);
            assertEquals(-1e12, spline.evaluate(1.0), 1.0);
            assertEquals(1e12, spline.evaluate(2.0), 1.0);
        }
    }
}
