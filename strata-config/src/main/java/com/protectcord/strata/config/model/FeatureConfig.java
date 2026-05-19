package com.protectcord.strata.config.model;

/**
 * Parsed feature placement configuration from TOML.
 *
 * @param step                       the generation step at which this feature is placed
 * @param count                      number of attempts per chunk
 * @param rarity                     chance of placement per attempt (0.0-1.0)
 * @param minHeight                  minimum y-level for placement
 * @param maxHeight                  maximum y-level for placement
 * @param block                      block type for ores and similar features
 * @param size                       vein size for ore features
 * @param discardChanceOnAirExposure chance to skip placement when exposed to air (0.0-1.0)
 */
public record FeatureConfig(
        String step,
        int count,
        double rarity,
        int minHeight,
        int maxHeight,
        String block,
        int size,
        double discardChanceOnAirExposure
) {

    /**
     * Vanilla-aligned feature generation steps.
     */
    public enum FeatureStep {
        RAW_GENERATION,
        LAKES,
        LOCAL_MODIFICATIONS,
        UNDERGROUND_STRUCTURES,
        SURFACE_STRUCTURES,
        STRONGHOLDS,
        UNDERGROUND_ORES,
        UNDERGROUND_DECORATION,
        FLUID_SPRINGS,
        VEGETAL_DECORATION,
        TOP_LAYER_MODIFICATION
    }
}
