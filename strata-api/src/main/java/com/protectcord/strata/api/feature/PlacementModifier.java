package com.protectcord.strata.api.feature;

/**
 * Built-in placement modifier types that control where features generate.
 */
public enum PlacementModifier {
    /** Place at a random position within a chunk. */
    RANDOM_OFFSET,

    /** Place at the heightmap surface. */
    HEIGHTMAP,

    /** Place at a fixed Y level. */
    FIXED_Y,

    /** Place between two Y levels uniformly. */
    UNIFORM_Y,

    /** Place with triangular distribution between two Y levels. */
    TRIANGLE_Y,

    /** Count-based repetition. */
    COUNT,

    /** Noise-threshold gating. */
    NOISE_THRESHOLD,

    /** Rarity filter (1-in-N chance per chunk). */
    RARITY_FILTER,

    /** Biome filter. */
    BIOME_FILTER,

    /** Block predicate filter. */
    BLOCK_PREDICATE,

    /** Surface water depth filter. */
    WATER_DEPTH_FILTER,

    /** In-square random spread. */
    IN_SQUARE
}
