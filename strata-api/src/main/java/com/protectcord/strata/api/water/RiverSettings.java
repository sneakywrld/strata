package com.protectcord.strata.api.water;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Configuration for the river generation system.
 *
 * <p>Rivers in Strata use macro-scale drainage basin computation followed by micro-scale
 * noise carving for natural-looking channels. This is part of the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#WATER_SYSTEM WATER_SYSTEM} stage.</p>
 *
 * @param enabled         whether river generation is active
 * @param riverWidth      base river width in blocks (valid range: 2-16)
 * @param riverDepth      base river depth in blocks (valid range: 2-8)
 * @param branchingFactor how aggressively rivers branch (0.0 = no branching, 1.0 = maximum)
 * @param meanderStrength how much rivers meander (0.0 = straight, 1.0 = highly sinuous)
 * @param noiseFunction   key of the {@link com.protectcord.strata.api.noise.NoiseFunction} for micro-scale variation
 * @param flowToOcean     whether rivers always terminate at ocean biomes
 * @param erosionStrength how much rivers erode surrounding terrain (0.0 = none, 1.0 = maximum)
 * @since 1.0.0
 * @see WaterSystemSettings
 */
public record RiverSettings(
        boolean enabled,
        int riverWidth,
        int riverDepth,
        double branchingFactor,
        double meanderStrength,
        NamespacedKey noiseFunction,
        boolean flowToOcean,
        double erosionStrength
) {

    /**
     * Returns default river settings: width 6, depth 4, moderate branching and meandering.
     *
     * @return sensible default river settings
     */
    public static RiverSettings defaults() {
        return new RiverSettings(
                true, 6, 4, 0.4, 0.6,
                NamespacedKey.strata("river_noise"),
                true, 0.3
        );
    }
}
