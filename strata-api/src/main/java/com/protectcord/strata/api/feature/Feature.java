package com.protectcord.strata.api.feature;

import com.protectcord.strata.api.chunk.BlockAccess;
import com.protectcord.strata.api.core.Keyed;

import java.util.Random;

/**
 * A world feature that places blocks at specific positions.
 * Features include trees, ores, vegetation, fluid springs, etc.
 */
public interface Feature extends Keyed {

    /**
     * The feature type for categorization.
     */
    FeatureType type();

    /**
     * The placement rules for this feature.
     */
    FeaturePlacement placement();

    /**
     * Places the feature at the given position.
     *
     * @param blocks access to the chunk's blocks
     * @param random seeded random for this position
     * @param x      world X coordinate
     * @param y      world Y coordinate
     * @param z      world Z coordinate
     * @return true if the feature was successfully placed
     */
    boolean place(BlockAccess blocks, Random random, int x, int y, int z);
}
