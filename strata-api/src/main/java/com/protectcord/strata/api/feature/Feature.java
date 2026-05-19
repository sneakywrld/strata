package com.protectcord.strata.api.feature;

import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.Keyed;

import java.util.Random;

/**
 * A world feature that places blocks at specific positions during the
 * {@link com.protectcord.strata.api.pipeline.GenerationStage#FEATURE_DECORATION FEATURE_DECORATION} stage.
 *
 * <p>Features encompass all decorative and resource-related block placements: trees, ores,
 * vegetation, fluid springs, geodes, boulders, and more. Each feature has a {@linkplain #type() type}
 * for categorization and {@linkplain #placement() placement rules} that control where it generates.</p>
 *
 * <p>Features are registered in the feature registry
 * ({@link com.protectcord.strata.api.core.StrataAPI#featureRegistry()}) and referenced by key
 * in {@link com.protectcord.strata.api.biome.Biome#features()}.</p>
 *
 * @since 1.0.0
 * @see FeatureType
 * @see FeaturePlacement
 * @see PlacementModifier
 */
public interface Feature extends Keyed {

    /**
     * Returns the type of this feature for categorization and default behavior selection.
     *
     * @return the {@link FeatureType}, never {@code null}
     */
    FeatureType type();

    /**
     * Returns the placement rules for this feature, including modifiers and parameters
     * that control where and how often the feature generates.
     *
     * @return the {@link FeaturePlacement}, never {@code null}
     */
    FeaturePlacement placement();

    /**
     * Places the feature at the given world position.
     *
     * <p>Implementations should use the provided {@link BlockAccess} to read existing blocks
     * and write new ones. The {@link Random} is seeded deterministically for this position
     * to ensure reproducible world generation.</p>
     *
     * @param blocks read-write access to block data in the chunk
     * @param random a seeded random instance for deterministic generation
     * @param x      the world X coordinate of the placement origin
     * @param y      the world Y coordinate of the placement origin
     * @param z      the world Z coordinate of the placement origin
     * @return {@code true} if the feature was successfully placed, {@code false} if placement
     *         was skipped (e.g., due to insufficient space or conflicting blocks)
     */
    boolean place(BlockAccess blocks, Random random, int x, int y, int z);
}
