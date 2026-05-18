package com.protectcord.strata.api.noise;

/**
 * Built-in noise algorithm types supported by the Strata noise engine.
 *
 * <p>Each constant corresponds to a well-known noise algorithm. The algorithm type is selected
 * in TOML configuration via the {@code type} field and determines the base noise pattern
 * before fractal settings ({@link FractalSettings}) and composite operations
 * ({@link CompositeNoise}) are applied.</p>
 *
 * @since 1.0.0
 * @see NoiseFunction
 * @see FractalSettings
 */
public enum NoiseType {
    /** Classic Perlin noise. */
    PERLIN,

    /** Simplex noise (Ken Perlin's improved algorithm). */
    SIMPLEX,

    /** OpenSimplex2 noise (patent-free simplex variant). */
    OPEN_SIMPLEX_2,

    /** OpenSimplex2S (smoother variant). */
    OPEN_SIMPLEX_2S,

    /** Cellular (Voronoi/Worley) noise. */
    CELLULAR,

    /** Value noise (interpolated random grid). */
    VALUE,

    /** Ridged multifractal noise. */
    RIDGED_MULTI,

    /** White noise (uniform random). */
    WHITE,

    /** Constant value (useful in composite chains). */
    CONSTANT
}
