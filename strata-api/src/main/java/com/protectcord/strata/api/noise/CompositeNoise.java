package com.protectcord.strata.api.noise;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * A noise function built by composing mathematical operations on a source {@link NoiseFunction}.
 *
 * <p>Composite noise enables powerful noise shaping without writing code. Operations are applied
 * sequentially, transforming the output of the source noise through operations like absolute value,
 * clamping, multiplication, spline mapping, and terracing.</p>
 *
 * <p>Example TOML configuration:</p>
 * <pre>
 * [noise.mountain_shape]
 * type = "composite"
 * source = "strata:continental_base"
 * operations = [
 *   { op = "abs" },
 *   { op = "multiply", value = 2.5 },
 *   { op = "clamp", min = 0.0, max = 1.0 },
 *   { op = "spline", points = [[0.0, 0.0], [0.3, 0.1], [0.7, 0.6], [1.0, 1.0]] }
 * ]
 * </pre>
 *
 * @since 1.0.0
 * @see NoiseFunction
 * @see Operation
 */
public interface CompositeNoise extends NoiseFunction {

    /**
     * Mathematical operations that can be applied in a composite noise chain.
     *
     * <p>Unary operations (e.g., {@link #ABS}, {@link #NEGATE}) transform a single value.
     * Binary operations (e.g., {@link #ADD}, {@link #MULTIPLY}) combine the noise value
     * with a constant or another noise source. Range operations (e.g., {@link #CLAMP})
     * constrain the output. Mapping operations (e.g., {@link #SPLINE}, {@link #TERRACE})
     * remap the value through control points.</p>
     *
     * @since 1.0.0
     */
    enum Operation {
        /** Absolute value: {@code |x|}. */
        ABS,
        /** Negation: {@code -x}. */
        NEGATE,
        /** Square: {@code x * x}. */
        SQUARE,
        /** Cube: {@code x * x * x}. */
        CUBE,
        /** Square root: {@code sqrt(x)} (input should be non-negative). */
        SQRT,
        /** Addition: {@code x + value}. */
        ADD,
        /** Multiplication: {@code x * value}. */
        MULTIPLY,
        /** Minimum: {@code min(x, value)}. */
        MIN,
        /** Maximum: {@code max(x, value)}. */
        MAX,
        /** Clamp: constrains {@code x} to {@code [min, max]}. */
        CLAMP,
        /** Linear interpolation between two values using {@code x} as the factor. */
        LERP,
        /** Cubic spline remapping through control points. */
        SPLINE,
        /** Terrace/plateau remapping through stepped control points. */
        TERRACE,
        /** Power: {@code x ^ value}. */
        POWER,
        /** Inversion: {@code 1.0 / x}. */
        INVERT
    }

    /**
     * Returns the key of the source {@link NoiseFunction} this composite is based on.
     *
     * <p>The source function's output is fed into the chain of {@link Operation}s
     * to produce the final composite noise value.</p>
     *
     * @return the source noise function key, never {@code null}
     */
    NamespacedKey source();
}
