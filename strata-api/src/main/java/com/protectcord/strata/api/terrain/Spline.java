package com.protectcord.strata.api.terrain;

import java.util.List;

/**
 * A cubic spline used for mapping continuous input values (e.g., noise, climate parameters)
 * to output values (e.g., terrain heights, density offsets) throughout the generation pipeline.
 *
 * <p>Splines provide smooth, designer-controllable mappings via a series of {@link Point control points}.
 * Between control points, values are interpolated using cubic Hermite interpolation, producing
 * smooth curves with controllable tangent slopes.</p>
 *
 * <p>Splines are referenced by key in {@link TerrainSettings} for continental and erosion shaping.</p>
 *
 * @since 1.0.0
 * @see TerrainSettings
 * @see Point
 */
public interface Spline {

    /**
     * A single control point on the spline curve.
     *
     * @param location   the input value (x-axis position on the spline)
     * @param value      the output value (y-axis) at this location
     * @param derivative the tangent slope at this point, controlling the curve shape
     *                   between adjacent points
     * @since 1.0.0
     */
    record Point(double location, double value, double derivative) {}

    /**
     * Evaluates the spline at the given input value using cubic Hermite interpolation.
     *
     * <p>For inputs outside the range of control points, the result is extrapolated
     * from the nearest endpoint's value and derivative.</p>
     *
     * @param input the input value to evaluate
     * @return the interpolated output value
     */
    double apply(double input);

    /**
     * Returns the ordered list of control points defining this spline.
     *
     * @return an unmodifiable list of {@link Point}s sorted by {@link Point#location()},
     *         never {@code null} or empty
     */
    List<Point> points();

    /**
     * Creates a spline from the given control points.
     *
     * <p><b>Note:</b> This factory method delegates to the {@code strata-core} implementation.
     * It will throw {@link UnsupportedOperationException} if called without the core module loaded.</p>
     *
     * @param points the control points (must contain at least one point)
     * @return a new spline instance
     * @throws UnsupportedOperationException if the core implementation is not available
     */
    static Spline of(List<Point> points) {
        // Implementation will be in strata-core
        throw new UnsupportedOperationException("Use core implementation");
    }
}
