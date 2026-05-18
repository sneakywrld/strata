package com.protectcord.strata.api.surface;

import com.protectcord.strata.api.block.StrataBlockState;
import com.protectcord.strata.api.core.Keyed;

import java.util.Optional;

/**
 * Determines which block to place at surface positions during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#SURFACE_BUILDING SURFACE_BUILDING} stage.
 *
 * <p>Surface rules are evaluated in {@linkplain #priority() priority} order (lower values first).
 * The first rule whose {@link SurfaceCondition} matches at a given position provides the block
 * for that position. If no rule matches, the default stone fill remains.</p>
 *
 * <p>Complex surfaces (e.g., grass on top, dirt underneath, sandstone in deserts) are built
 * by layering multiple rules with different conditions and priorities. Rules are registered
 * in the surface rule registry
 * ({@link com.protectcord.strata.api.core.StrataAPI#surfaceRuleRegistry()}).</p>
 *
 * @since 1.0.0
 * @see SurfaceCondition
 * @see com.protectcord.strata.api.block.StrataBlockState
 */
public interface SurfaceRule extends Keyed {

    /**
     * Attempts to determine the block for the given surface position.
     *
     * <p>If this rule's conditions are satisfied by the context, it returns the block state
     * to place. Otherwise, it returns {@link Optional#empty()} and the next rule in priority
     * order is evaluated.</p>
     *
     * @param context the surface evaluation context containing position, biome, depth, and slope data
     * @return the block to place, or empty if this rule does not apply at this position
     */
    Optional<StrataBlockState> apply(SurfaceCondition.SurfaceContext context);

    /**
     * Returns the priority of this rule. Lower values are evaluated first.
     *
     * <p>Rules with the same priority have undefined evaluation order.</p>
     *
     * @return the priority value
     */
    int priority();
}
