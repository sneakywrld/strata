package com.protectcord.strata.api.pipeline;

/**
 * Semantic classification of generation pipeline stages.
 *
 * <p>While {@link GenerationStage} defines the ordered execution steps, this enum provides
 * a semantic type for each stage that can be used for filtering, grouping, and reporting.</p>
 *
 * @since 1.0.0
 * @see GenerationStage
 * @see PipelineAccessor
 */
public enum GenerationStageType {

    INITIALIZATION,
    CONTINENTAL_SHAPE,
    CLIMATE_SAMPLING,
    BIOME_ASSIGNMENT,
    TERRAIN_SHAPING,
    AQUIFER_PLACEMENT,
    SURFACE_BUILDING,
    CARVING,
    WATER_SYSTEM,
    STRUCTURE_GENERATION,
    FEATURE_DECORATION,
    ENTITY_SPAWNING,
    LIGHTING,
    FINALIZATION
}
