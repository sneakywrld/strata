package com.protectcord.strata.api.noise;

import com.protectcord.strata.api.core.Keyed;

/**
 * A noise function that produces deterministic pseudo-random values for given coordinates.
 *
 * <p>All procedural noise in Strata is built from composable {@code NoiseFunction} instances.
 * Noise functions are registered in the noise registry
 * ({@link com.protectcord.strata.api.core.StrataAPI#noiseRegistry()}) and referenced by key
 * throughout the generation system -- in terrain settings, climate sampling, water systems,
 * and composite noise chains.</p>
 *
 * <p>Implementations must be deterministic: the same coordinates and seed must always produce
 * the same output value. Output values should lie within the range
 * [{@link #minValue()}, {@link #maxValue()}].</p>
 *
 * @since 1.0.0
 * @see NoiseType
 * @see CompositeNoise
 * @see FractalSettings
 */
public interface NoiseFunction extends Keyed {

    /**
     * Samples the noise at a 2D position (XZ plane).
     *
     * <p>Typically used for surface-level sampling such as biome climate parameters,
     * continental shape, and heightmap generation.</p>
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @return the noise value, within [{@link #minValue()}, {@link #maxValue()}]
     */
    double sample(double x, double z);

    /**
     * Samples the noise at a 3D position.
     *
     * <p>Used for volumetric generation such as 3D density functions, cave carving,
     * and aquifer placement.</p>
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return the noise value, within [{@link #minValue()}, {@link #maxValue()}]
     */
    double sample(double x, double y, double z);

    /**
     * Returns the minimum possible output value of this noise function.
     *
     * @return the lower bound of the output range
     */
    double minValue();

    /**
     * Returns the maximum possible output value of this noise function.
     *
     * @return the upper bound of the output range
     */
    double maxValue();
}
