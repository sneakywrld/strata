package com.protectcord.strata.api.terrain;

import com.protectcord.strata.api.core.Keyed;

/**
 * A 3D density function used for terrain shaping during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#TERRAIN_SHAPING TERRAIN_SHAPING} stage.
 *
 * <p>Density functions define the shape of terrain in three dimensions. The convention is:</p>
 * <ul>
 *   <li>Positive values represent solid terrain (stone, etc.)</li>
 *   <li>Negative values represent air</li>
 *   <li>The terrain surface lies at the zero-crossing</li>
 * </ul>
 *
 * <p>Density functions are registered in the noise registry and referenced by key in
 * {@link TerrainSettings#densityFunction()}. They are typically built from noise functions,
 * splines, and height offsets.</p>
 *
 * @since 1.0.0
 * @see TerrainSettings
 * @see com.protectcord.strata.api.noise.NoiseFunction
 */
public interface DensityFunction extends Keyed {

    /**
     * Computes the density at the given world coordinates.
     *
     * @param x the world X coordinate
     * @param y the world Y coordinate
     * @param z the world Z coordinate
     * @return the density value; positive indicates solid terrain, negative indicates air
     */
    double compute(int x, int y, int z);

    /**
     * Returns the minimum possible density value this function can produce.
     *
     * @return the lower bound of the density range
     */
    double minValue();

    /**
     * Returns the maximum possible density value this function can produce.
     *
     * @return the upper bound of the density range
     */
    double maxValue();
}
