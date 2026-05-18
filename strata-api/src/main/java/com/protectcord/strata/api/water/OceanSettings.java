package com.protectcord.strata.api.water;

import com.protectcord.strata.api.core.NamespacedKey;

/**
 * Configuration for ocean generation within the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#WATER_SYSTEM WATER_SYSTEM} stage.
 *
 * <p>Controls continental shelf bathymetry, ocean floor variation, and toggles for
 * underwater features such as coral reefs, kelp forests, ocean monuments, shipwrecks,
 * and deep ocean trenches.</p>
 *
 * @param enabled            whether ocean generation is active
 * @param shelfDepth         depth of the continental shelf in blocks below sea level
 * @param deepOceanDepth     depth of the deep ocean floor in blocks below sea level
 * @param floorNoiseFunction key of the {@link com.protectcord.strata.api.noise.NoiseFunction} for ocean floor variation
 * @param coralReefs         whether to generate coral reef features
 * @param kelpForests        whether to generate kelp forest features
 * @param oceanMonuments     whether to generate ocean monument structures
 * @param shipwrecks         whether to generate shipwreck structures
 * @param trenchGeneration   whether to generate deep ocean trenches
 * @param trenchMaxDepth     maximum depth of ocean trenches in blocks below sea level
 * @since 1.0.0
 * @see WaterSystemSettings
 */
public record OceanSettings(
        boolean enabled,
        int shelfDepth,
        int deepOceanDepth,
        NamespacedKey floorNoiseFunction,
        boolean coralReefs,
        boolean kelpForests,
        boolean oceanMonuments,
        boolean shipwrecks,
        boolean trenchGeneration,
        int trenchMaxDepth
) {

    /**
     * Returns default ocean settings with all features enabled.
     *
     * @return sensible default ocean settings
     */
    public static OceanSettings defaults() {
        return new OceanSettings(
                true, 15, 45,
                NamespacedKey.strata("ocean_floor_noise"),
                true, true, true, true,
                true, 60
        );
    }
}
