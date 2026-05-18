package com.protectcord.strata.api.surface;

import com.protectcord.strata.api.biome.Biome;

/**
 * A predicate that determines whether a {@link SurfaceRule} should apply at a given position.
 *
 * <p>Conditions can be composed using boolean logic (AND, OR, NOT) to build complex
 * surface selection criteria. Common conditions include biome checks, depth-below-surface
 * thresholds, slope ranges, and underwater detection.</p>
 *
 * @since 1.0.0
 * @see SurfaceRule
 * @see SurfaceContext
 */
public interface SurfaceCondition {

    /**
     * Evaluates whether this condition is met at the given surface position.
     *
     * @param context the surface evaluation context containing position, biome, and environmental data
     * @return {@code true} if the condition is satisfied, {@code false} otherwise
     */
    boolean test(SurfaceContext context);

    /**
     * Contextual data provided to {@link SurfaceCondition}s and {@link SurfaceRule}s during
     * surface block evaluation.
     *
     * @param x                 the world X coordinate of the block being evaluated
     * @param y                 the world Y coordinate of the block being evaluated
     * @param z                 the world Z coordinate of the block being evaluated
     * @param surfaceY          the Y coordinate of the terrain surface at this XZ column
     * @param depthBelowSurface how many blocks below the surface this position is (0 = surface)
     * @param slope             the terrain slope at this position (0.0 = flat, higher = steeper)
     * @param biome             the {@link Biome} assigned to this position
     * @param underwater        {@code true} if this position is below the water surface
     * @param waterDepth        how many blocks of water are above this position (0 if not underwater)
     * @param seed              the world seed, for deterministic randomization in conditions
     * @since 1.0.0
     */
    record SurfaceContext(
            int x, int y, int z,
            int surfaceY,
            int depthBelowSurface,
            double slope,
            Biome biome,
            boolean underwater,
            int waterDepth,
            long seed
    ) {}
}
