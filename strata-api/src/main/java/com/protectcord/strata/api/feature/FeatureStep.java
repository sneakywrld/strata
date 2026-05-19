package com.protectcord.strata.api.feature;

/**
 * Ordered decoration steps within the feature decoration stage.
 *
 * <p>Features are placed in step order during
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#FEATURE_DECORATION FEATURE_DECORATION}.
 * Each step groups related feature types to ensure correct placement ordering
 * (e.g., ores before vegetation, surface decoration after trees).</p>
 *
 * @since 1.0.0
 * @see Feature
 * @see FeaturePlacement
 */
public enum FeatureStep {

    /** Raw terrain modifications applied before other features. */
    RAW,

    /** Ore vein placement. */
    ORES,

    /** Underground decorations (cave vines, glow lichen, dripstone). */
    UNDERGROUND_DECORATION,

    /** Vegetation placement (trees, bamboo, mushrooms). */
    VEGETAL,

    /** Surface-level decorations (flowers, grass, rocks). */
    SURFACE_DECORATION,

    /** Fluid spring placement (water and lava sources). */
    FLUID_SPRINGS,

    /** Final top-layer features (snow, ice, freeze). */
    TOP_LAYER
}
