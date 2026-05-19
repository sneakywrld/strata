package com.protectcord.strata.api.feature;

/**
 * Built-in feature types for categorization and default behavior selection.
 *
 * <p>Each constant represents a category of world feature. The type determines default
 * generation logic and can be used by third-party plugins to filter or categorize features.
 * Use {@link #CUSTOM} for plugin-defined feature types that do not fit standard categories.</p>
 *
 * @since 1.0.0
 * @see Feature#type()
 */
public enum FeatureType {
    /** Ore vein generation. */
    ORE,

    /** Tree generation (all tree types). */
    TREE,

    /** Small vegetation (flowers, grass, ferns). */
    VEGETATION,

    /** Fluid springs (water, lava). */
    FLUID_SPRING,

    /** Snow and ice placement. */
    SNOW_ICE,

    /** Mushroom generation (small and huge). */
    MUSHROOM,

    /** Bamboo generation. */
    BAMBOO,

    /** Cactus generation. */
    CACTUS,

    /** Sugar cane generation. */
    SUGAR_CANE,

    /** Kelp and seagrass. */
    UNDERWATER_VEGETATION,

    /** Coral generation. */
    CORAL,

    /** Amethyst geode. */
    GEODE,

    /** Sculk patch. */
    SCULK,

    /** Nether vegetation (crimson/warped fungi, nether sprouts). */
    NETHER_VEGETATION,

    /** End gateway, chorus plant. */
    END_FEATURE,

    /** Disk (sand, gravel, clay patches). */
    DISK,

    /** Boulder/rock feature. */
    BOULDER,

    /** Dripstone cluster. */
    DRIPSTONE,

    /** Glow lichen, cave vines. */
    CAVE_DECORATION,

    /** Custom feature type (for plugins). */
    CUSTOM
}
